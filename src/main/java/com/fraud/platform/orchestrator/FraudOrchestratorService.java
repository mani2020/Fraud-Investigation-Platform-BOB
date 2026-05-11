package com.fraud.platform.orchestrator;

import com.fraud.platform.agents.*;
import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.FraudAlert;
import com.fraud.platform.model.FraudDecision;
import com.fraud.platform.service.DecisionService;
import com.fraud.platform.service.ExplainabilityService;
import com.fraud.platform.service.FraudNotificationService;
import com.fraud.platform.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

/**
 * Orchestrates fraud detection by coordinating multiple fraud analysis agents.
 * Aggregates agent results into a final fraud decision with explainable AI.
 */
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

    // Thread pool for parallel agent execution
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * Analyze transaction using all fraud detection agents.
     * Agents run in parallel for optimal performance.
     *
     * @param transaction Transaction event to analyze
     * @return Final fraud decision with aggregated results
     */
    public FraudDecision analyzeTransaction(TransactionEvent transaction) {
        long startTime = System.currentTimeMillis();
        log.info("Starting fraud analysis for transaction: {}", transaction.getTxnId());

        try {
            // Execute all agents in parallel
            CompletableFuture<AgentResult> riskFuture = CompletableFuture.supplyAsync(
                    () -> riskAgent.analyze(transaction), executorService);
            
            CompletableFuture<AgentResult> geoFuture = CompletableFuture.supplyAsync(
                    () -> geoAgent.analyze(transaction), executorService);
            
            CompletableFuture<AgentResult> deviceFuture = CompletableFuture.supplyAsync(
                    () -> deviceAgent.analyze(transaction), executorService);
            
            CompletableFuture<AgentResult> amlFuture = CompletableFuture.supplyAsync(
                    () -> amlAgent.analyze(transaction), executorService);
            
            CompletableFuture<AgentResult> behaviorFuture = CompletableFuture.supplyAsync(
                    () -> behaviorAgent.analyze(transaction), executorService);

            // Wait for all agents to complete
            CompletableFuture.allOf(riskFuture, geoFuture, deviceFuture, amlFuture, behaviorFuture).join();

            // Collect agent results
            List<AgentResult> agentResults = new ArrayList<>();
            agentResults.add(riskFuture.get());
            agentResults.add(geoFuture.get());
            agentResults.add(deviceFuture.get());
            agentResults.add(amlFuture.get());
            agentResults.add(behaviorFuture.get());

            // Aggregate results into final decision
            FraudDecision decision = aggregateResults(transaction.getTxnId(), agentResults);
            decision.setTimestamp(LocalDateTime.now());
            decision.setTotalProcessingTimeMs(System.currentTimeMillis() - startTime);

            // Make final decision using DecisionService
            String finalDecision = decisionService.makeDecision(decision);
            decision.setDecision(finalDecision);

            // Generate human-readable explanations
            String explanation = explainabilityService.generateExplanation(decision);
            String summary = explainabilityService.generateShortSummary(decision);
            decision.setHumanReadableExplanation(explanation);
            decision.setShortSummary(summary);

            // Log decision matrix
            String decisionMatrix = decisionService.generateSimpleTable(decision);
            log.info("Fraud analysis completed for {}:\n{}", transaction.getTxnId(), decisionMatrix);
            log.info("Summary: {}", summary);

            // Update transaction with fraud decision
            updateTransaction(transaction.getTxnId(), decision);

            // Create fraud alert for dashboard
            createFraudAlert(decision);

            return decision;

        } catch (Exception e) {
            log.error("Error during fraud analysis for transaction: {}", transaction.getTxnId(), e);
            
            // Return safe default decision on error
            return FraudDecision.builder()
                    .txnId(transaction.getTxnId())
                    .finalScore(BigDecimal.valueOf(100))
                    .decision("REVIEW")
                    .allReasons(List.of("Error during fraud analysis: " + e.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .totalProcessingTimeMs(System.currentTimeMillis() - startTime)
                    .confidence(BigDecimal.ZERO)
                    .build();
        }
    }

    /**
     * Aggregate agent results into final fraud decision.
     * Uses weighted average of agent scores.
     *
     * @param txnId Transaction ID
     * @param agentResults Results from all agents
     * @return Final fraud decision
     */
    private FraudDecision aggregateResults(String txnId, List<AgentResult> agentResults) {
        FraudDecision decision = FraudDecision.builder()
                .txnId(txnId)
                .agentResults(new ArrayList<>())
                .allReasons(new ArrayList<>())
                .riskFactors(new HashMap<>())
                .build();

        // Calculate weighted average score
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalConfidence = BigDecimal.ZERO;

        Map<String, Integer> decisionCounts = new HashMap<>();
        decisionCounts.put("APPROVE", 0);
        decisionCounts.put("REVIEW", 0);
        decisionCounts.put("REJECT", 0);

        for (AgentResult result : agentResults) {
            decision.addAgentResult(result);

            // Get agent weight
            double weight = getAgentWeight(result.getAgentName());
            BigDecimal agentWeight = BigDecimal.valueOf(weight);

            // Calculate weighted score
            BigDecimal weightedScore = result.getRiskScore().multiply(agentWeight);
            totalWeightedScore = totalWeightedScore.add(weightedScore);
            totalWeight = totalWeight.add(agentWeight);

            // Aggregate confidence
            totalConfidence = totalConfidence.add(result.getConfidence());

            // Count decisions
            String agentDecision = result.getDecision();
            decisionCounts.put(agentDecision, decisionCounts.get(agentDecision) + 1);

            // Add to risk factors
            decision.getRiskFactors().put(result.getAgentName() + "_score", result.getRiskScore());
            decision.getRiskFactors().put(result.getAgentName() + "_decision", result.getDecision());
        }

        // Calculate final score (weighted average)
        BigDecimal finalScore = totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
        decision.setFinalScore(finalScore);

        // Calculate average confidence
        BigDecimal avgConfidence = totalConfidence.divide(
                BigDecimal.valueOf(agentResults.size()), 2, RoundingMode.HALF_UP);
        decision.setConfidence(avgConfidence);

        // Determine final decision based on score and agent consensus
        String finalDecision = determineFinalDecision(finalScore, decisionCounts);
        decision.setDecision(finalDecision);

        return decision;
    }

    /**
     * Determine final decision based on score and agent consensus.
     *
     * @param finalScore Weighted average score
     * @param decisionCounts Count of each decision type
     * @return Final decision
     */
    private String determineFinalDecision(BigDecimal finalScore, Map<String, Integer> decisionCounts) {
        // If any agent says REJECT and score is high, reject
        if (decisionCounts.get("REJECT") > 0 && finalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "REJECT";
        }

        // If majority says REJECT, reject
        if (decisionCounts.get("REJECT") >= 3) {
            return "REJECT";
        }

        // If score is very high, reject regardless of agent decisions
        if (finalScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "REJECT";
        }

        // If score is moderate or any agent says REVIEW, review
        if (finalScore.compareTo(BigDecimal.valueOf(40)) >= 0 || decisionCounts.get("REVIEW") > 0) {
            return "REVIEW";
        }

        // Otherwise approve
        return "APPROVE";
    }

    /**
     * Get agent weight by name.
     *
     * @param agentName Agent name
     * @return Agent weight
     */
    private double getAgentWeight(String agentName) {
        return switch (agentName) {
            case "RiskAgent" -> riskAgent.getWeight();
            case "GeoAgent" -> geoAgent.getWeight();
            case "DeviceAgent" -> deviceAgent.getWeight();
            case "AMLAgent" -> amlAgent.getWeight();
            case "BehaviorAgent" -> behaviorAgent.getWeight();
            default -> 0.2; // Default weight
        };
    }

    /**
     * Update transaction with fraud decision.
     *
     * @param txnId Transaction ID
     * @param decision Fraud decision
     */
    private void updateTransaction(String txnId, FraudDecision decision) {
        try {
            transactionService.updateFraudDecisionWithDetails(
                txnId,
                decision.getDecision(),
                decision.getFinalScore(),
                decision.getAgentResults(),
                decision.getAllReasons()
            );
            log.info("Updated transaction {} with fraud decision and agent results: {}", txnId, decision.getDecision());
        } catch (Exception e) {
            log.error("Failed to update transaction {} with fraud decision", txnId, e);
        }
    }

    /**
     * Create fraud alert for dashboard notifications.
     *
     * @param decision Fraud decision
     */
    private void createFraudAlert(FraudDecision decision) {
        try {
            // Only create alerts for REVIEW and REJECT decisions
            if ("REVIEW".equals(decision.getDecision()) || "REJECT".equals(decision.getDecision())) {
                FraudAlert alert = notificationService.createAlert(decision);
                log.info("Fraud alert created: alertId={}, severity={}, txnId={}",
                         alert.getAlertId(), alert.getSeverity(), decision.getTxnId());
            }
        } catch (Exception e) {
            log.error("Failed to create fraud alert for transaction: {}", decision.getTxnId(), e);
            // Don't fail the fraud detection if alert creation fails
        }
    }

    /**
     * Shutdown executor service gracefully.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}

// Made with Bob