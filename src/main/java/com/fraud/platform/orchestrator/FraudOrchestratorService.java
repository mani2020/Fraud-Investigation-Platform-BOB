package com.fraud.platform.orchestrator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fraud.platform.agents.AMLAgent;
import com.fraud.platform.agents.BehaviorAgent;
import com.fraud.platform.agents.DeviceAgent;
import com.fraud.platform.agents.GeoAgent;
import com.fraud.platform.agents.RiskAgent;
import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.CanonicalFraudEvent;
import com.fraud.platform.model.FraudAlert;
import com.fraud.platform.model.FraudDecision;
import com.fraud.platform.service.DecisionService;
import com.fraud.platform.service.ExplainabilityService;
import com.fraud.platform.service.FraudNotificationService;
import com.fraud.platform.service.ICAContextService;
import com.fraud.platform.service.TransactionService;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudOrchestratorService {

    private final RiskAgent riskAgent;
    private final GeoAgent geoAgent;
    private final DeviceAgent deviceAgent;
    private final AMLAgent amlAgent;
    private final BehaviorAgent behaviorAgent;

    private final TransactionService transactionService;
    private final ExplainabilityService explainabilityService;
    private final DecisionService decisionService;
    private final FraudNotificationService notificationService;
    private final ICAContextService icaContextService;

    /**
     * Thread pool for parallel fraud agent execution.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * Analyze transaction using all fraud detection agents.
     * Agents run in parallel for optimal performance.
     *
     * @param event Canonical fraud event
     * @return Final fraud decision
     */
    @Transactional(rollbackFor = Exception.class)
    public FraudDecision analyzeTransaction(
            CanonicalFraudEvent event) {

        long startTime = System.currentTimeMillis();

        log.info(
                "Starting fraud analysis for transaction: {}",
                event.getTxnId());

        try {

            /*
             * Execute agents in parallel
             */
            CompletableFuture<AgentResult> riskFuture = CompletableFuture.supplyAsync(
                    () -> riskAgent.analyze(event),
                    executorService);

            CompletableFuture<AgentResult> geoFuture = CompletableFuture.supplyAsync(
                    () -> geoAgent.analyze(event),
                    executorService);

            CompletableFuture<AgentResult> deviceFuture = CompletableFuture.supplyAsync(
                    () -> deviceAgent.analyze(event),
                    executorService);

            CompletableFuture<AgentResult> amlFuture = CompletableFuture.supplyAsync(
                    () -> amlAgent.analyze(event),
                    executorService);

            CompletableFuture<AgentResult> behaviorFuture = CompletableFuture.supplyAsync(
                    () -> behaviorAgent.analyze(event),
                    executorService);

            /*
             * Wait for all agents
             */
            CompletableFuture.allOf(
                    riskFuture,
                    geoFuture,
                    deviceFuture,
                    amlFuture,
                    behaviorFuture).get(5, TimeUnit.SECONDS);

            /*
             * Collect results
             */
            List<AgentResult> agentResults = new ArrayList<>();

            agentResults.add(riskFuture.get());
            agentResults.add(geoFuture.get());
            agentResults.add(deviceFuture.get());
            agentResults.add(amlFuture.get());
            agentResults.add(behaviorFuture.get());

            /*
             * Aggregate results
             */
            FraudDecision decision = aggregateResults(
                    event.getTxnId(),
                    agentResults);

            decision.setTimestamp(LocalDateTime.now());

            decision.setTotalProcessingTimeMs(
                    System.currentTimeMillis() - startTime);

            /*
             * Final decision
             */
            String finalDecision = decisionService.makeDecision(decision);

            decision.setDecision(finalDecision);

            /*
             * Explainability
             */
            String explanation = explainabilityService
                    .generateExplanation(decision);

            String summary = explainabilityService
                    .generateShortSummary(decision);

            decision.setHumanReadableExplanation(
                    explanation);

            decision.setShortSummary(summary);

            /*
             * Log matrix
             */
            String decisionMatrix = decisionService
                    .generateSimpleTable(decision);

            log.info(
                    "Fraud analysis completed for {}:\n{}",
                    event.getTxnId(),
                    decisionMatrix);

            log.info("Summary: {}", summary);

            /*
             * DB updates
             */
            updateTransaction(
                    event.getTxnId(),
                    decision);

            createFraudAlert(decision);

            /*
             * ICA ingestion after successful commit
             */
            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {

                                    CompletableFuture.runAsync(() -> {

                                        try {

                                            icaContextService
                                                    .ingestTransaction(
                                                            event,
                                                            decision);

                                            log.info(
                                                    "ICA ingestion completed for txn: {}",
                                                    event.getTxnId());

                                        } catch (Exception ex) {

                                            log.error(
                                                    "ICA ingestion failed for txn: {}",
                                                    event.getTxnId(),
                                                    ex);
                                        }

                                    });
                                }
                            });

            return decision;

        } catch (Exception e) {

            log.error(
                    "Error during fraud analysis for transaction: {}",
                    event.getTxnId(),
                    e);

            /*
             * Force rollback
             */
            throw new RuntimeException(
                    "Fraud analysis failed",
                    e);
        }
    }

    /**
     * Aggregate all agent results.
     */
    private FraudDecision aggregateResults(
            String txnId,
            List<AgentResult> agentResults) {

        FraudDecision decision = FraudDecision.builder()
                .txnId(txnId)
                .agentResults(new ArrayList<>())
                .allReasons(new ArrayList<>())
                .riskFactors(new HashMap<>())
                .build();

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalConfidence = BigDecimal.ZERO;

        Map<String, Integer> decisionCounts = new HashMap<>();

        decisionCounts.put("APPROVE", 0);
        decisionCounts.put("OTP", 0);
        decisionCounts.put("HOLD", 0);
        decisionCounts.put("BLOCK", 0);

        for (AgentResult result : agentResults) {

            decision.addAgentResult(result);

            double weight = getAgentWeight(result.getAgentName());

            BigDecimal agentWeight = BigDecimal.valueOf(weight);

            BigDecimal weightedScore = result.getRiskScore()
                    .multiply(agentWeight);

            totalWeightedScore = totalWeightedScore.add(weightedScore);

            totalWeight = totalWeight.add(agentWeight);

            totalConfidence = totalConfidence.add(
                    result.getConfidence());

            String agentDecision = result.getDecision();

            decisionCounts.put(
                    agentDecision,
                    decisionCounts.getOrDefault(agentDecision, 0) + 1);

            decision.getRiskFactors().put(
                    result.getAgentName() + "_score",
                    result.getRiskScore());

            decision.getRiskFactors().put(
                    result.getAgentName() + "_decision",
                    result.getDecision());

            /*
             * Critical fraud indicators
             */
            for (String reason : result.getReasons()) {

                String lower = reason.toLowerCase();

                if (lower.contains("blacklisted merchant")) {
                    decision.getRiskFactors().put("blacklistedMerchant", true);
                }

                if (lower.contains("blacklisted ip")) {
                    decision.getRiskFactors().put("blacklistedIp", true);
                }

                if (lower.contains("vpn")) {
                    decision.getRiskFactors().put("vpnDetected", true);
                }

                if (lower.contains("proxy")) {
                    decision.getRiskFactors().put("proxyDetected", true);
                }

                if (lower.contains("tor")) {
                    decision.getRiskFactors().put("torDetected", true);
                }
            }
        }

        // Calculate final score and confidence
        BigDecimal finalScore = totalWeightedScore.divide(
                totalWeight,
                2,
                RoundingMode.HALF_UP);

        if (finalScore.compareTo(
                BigDecimal.valueOf(100)) > 0) {

            finalScore = BigDecimal.valueOf(100);
        }

        decision.setFinalScore(finalScore);

        BigDecimal avgConfidence = totalConfidence.divide(
                BigDecimal.valueOf(agentResults.size()),
                2,
                RoundingMode.HALF_UP);

        decision.setConfidence(avgConfidence);

        return decision;
    }

    /**
     * Agent weight lookup.
     */
    private double getAgentWeight(String agentName) {

        return switch (agentName) {

            case "RiskAgent" ->
                riskAgent.getWeight();

            case "GeoAgent" ->
                geoAgent.getWeight();

            case "DeviceAgent" ->
                deviceAgent.getWeight();

            case "AMLAgent" ->
                amlAgent.getWeight();

            case "BehaviorAgent" ->
                behaviorAgent.getWeight();

            default -> 0.2;
        };
    }

    /**
     * Update transaction details.
     */
    private void updateTransaction(
            String txnId,
            FraudDecision decision) {

        transactionService
                .updateFraudDecisionWithDetails(
                        txnId,
                        decision.getDecision(),
                        decision.getFinalScore(),
                        decision.getAgentResults(),
                        decision.getAllReasons());

        log.info(
                "Updated transaction {} with fraud decision {}",
                txnId,
                decision.getDecision());
    }

    /**
     * Create fraud alert.
     */
    private void createFraudAlert(
            FraudDecision decision) {

        String decisionType = decision.getDecision();

        BigDecimal score = decision.getFinalScore();

        if ("BLOCK".equals(decisionType)
                || "HOLD".equals(decisionType)
                || ("OTP".equals(decisionType)
                        && score.compareTo(
                                BigDecimal.valueOf(50)) >= 0)) {

            FraudAlert alert = notificationService
                    .createAlert(decision);

            log.info(
                    "Fraud alert created: alertId={}, severity={}, decision={}, txnId={}",
                    alert.getAlertId(),
                    alert.getSeverity(),
                    decisionType,
                    decision.getTxnId());
        }
    }

    /**
     * Graceful shutdown.
     */
    @PreDestroy
    public void shutdown() {

        log.info(
                "Shutting down fraud orchestrator executor service");

        executorService.shutdown();
    }
}