# Fraud Investigation Timeline Visualization

Visual representation of the fraud detection process showing agent execution flow and analysis stages.

---

## Complete Investigation Timeline

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                    FRAUD INVESTIGATION TIMELINE                                     │
│                    Transaction: TXN-002 | Amount: $50,000                          │
└─────────────────────────────────────────────────────────────────────────────────────┘

TIME: 0ms
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 1: TRANSACTION RECEIVED                                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ✓ Transaction submitted via API                                                     │
│ ✓ Saved to PostgreSQL (status=PENDING)                                            │
│ ✓ Published to Kafka topic: "fraud-transactions"                                   │
│ → API Response: 201 Created (< 100ms)                                              │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    ↓
TIME: 50ms
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 2: KAFKA CONSUMER PICKUP                                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ✓ Event consumed from Kafka                                                        │
│ ✓ FraudOrchestratorService triggered                                               │
│ ✓ Parallel agent execution initiated                                               │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    ↓
TIME: 100ms - 400ms (Parallel Execution)
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 3: PARALLEL AGENT ANALYSIS                                                    │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  Thread 1          Thread 2          Thread 3          Thread 4          Thread 5  │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐│
│  │  RISK    │     │   GEO    │     │  DEVICE  │     │   AML    │     │ BEHAVIOR ││
│  │  AGENT   │     │  AGENT   │     │  AGENT   │     │  AGENT   │     │  AGENT   ││
│  └──────────┘     └──────────┘     └──────────┘     └──────────┘     └──────────┘│
│       ↓                ↓                ↓                ↓                ↓        │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐│
│  │ Analyze  │     │ Analyze  │     │ Analyze  │     │ Analyze  │     │ Analyze  ││
│  │ Amount   │     │ Country  │     │ Device   │     │Watchlist │     │ Pattern  ││
│  │ Velocity │     │ Location │     │Fingerprint│    │Screening │     │ Analysis ││
│  │ Patterns │     │ Travel   │     │ Trust    │     │ AML      │     │ Behavior ││
│  └──────────┘     └──────────┘     └──────────┘     └──────────┘     └──────────┘│
│       ↓                ↓                ↓                ↓                ↓        │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐│
│  │ Score:   │     │ Score:   │     │ Score:   │     │ Score:   │     │ Score:   ││
│  │  85.0    │     │  90.0    │     │  75.0    │     │  60.0    │     │  80.0    ││
│  │ REJECT   │     │ REJECT   │     │ REVIEW   │     │ REVIEW   │     │ REJECT   ││
│  │ 120ms    │     │  95ms    │     │ 110ms    │     │ 150ms    │     │ 105ms    ││
│  └──────────┘     └──────────┘     └──────────┘     └──────────┘     └──────────┘│
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    ↓
TIME: 450ms
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 4: RESULT AGGREGATION                                                         │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ✓ All 5 agent results collected                                                    │
│ ✓ Weighted score calculation:                                                      │
│   • Risk Agent:     85.0 × 0.25 = 21.25                                           │
│   • Geo Agent:      90.0 × 0.20 = 18.00                                           │
│   • Device Agent:   75.0 × 0.20 = 15.00                                           │
│   • AML Agent:      60.0 × 0.20 = 12.00                                           │
│   • Behavior Agent: 80.0 × 0.15 = 12.00                                           │
│   ─────────────────────────────────────                                            │
│   Final Score: 78.25                                                               │
│                                                                                     │
│ ✓ Agent consensus analysis:                                                        │
│   • REJECT votes: 3 (Risk, Geo, Behavior)                                         │
│   • REVIEW votes: 2 (Device, AML)                                                 │
│   • APPROVE votes: 0                                                               │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    ↓
TIME: 480ms
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 5: DECISION MAKING                                                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ✓ DecisionService.makeDecision() invoked                                           │
│ ✓ Decision logic applied:                                                          │
│   • Final Score: 78.25 >= 70 → REJECT                                             │
│   • REJECT votes: 3 >= 3 → REJECT                                                 │
│   • Confidence: 92%                                                                │
│                                                                                     │
│ → FINAL DECISION: REJECT                                                           │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    ↓
TIME: 500ms
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 6: EXPLAINABILITY GENERATION                                                  │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ✓ ExplainabilityService.generateExplanation() invoked                              │
│ ✓ Human-readable explanation created:                                              │
│                                                                                     │
│   "HIGH FRAUD RISK DETECTED                                                        │
│                                                                                     │
│   Key Risk Factors:                                                                │
│   1. Geographic Risk (35%) - Transaction from Nigeria                             │
│   2. Transaction Amount (30%) - $50,000 exceeds average by 400%                   │
│   3. Device Fingerprint (20%) - Unknown device                                    │
│   4. Behavioral Anomaly (15%) - Transaction at 3:00 AM                            │
│                                                                                     │
│   Recommendation: REJECT and contact customer"                                     │
│                                                                                     │
│ ✓ Short summary generated                                                          │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    ↓
TIME: 520ms
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 7: DATABASE UPDATE                                                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ✓ Transaction updated in PostgreSQL:                                               │
│   • fraudDecision: "REJECT"                                                        │
│   • fraudScore: 78.25                                                              │
│   • status: "PROCESSED"                                                            │
│   • processingTime: 520ms                                                          │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    ↓
TIME: 540ms
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 8: ALERT CREATION                                                             │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ✓ FraudNotificationService.createAlert() invoked                                   │
│ ✓ Alert created:                                                                   │
│   • alertId: ALERT-1234567890-123                                                  │
│   • severity: CRITICAL                                                             │
│   • message: "Fraud detected - Score: 78.3"                                       │
│   • txnId: TXN-002                                                                 │
│   • timestamp: 2026-05-11T14:30:00                                                │
│                                                                                     │
│ ✓ Alert stored in memory (last 1000 alerts)                                       │
│ ✓ Alert available via API: GET /api/alerts/critical                               │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                    ↓
TIME: 560ms
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STAGE 9: KAFKA ACKNOWLEDGMENT                                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ✓ Kafka message acknowledged                                                       │
│ ✓ Processing complete                                                              │
│                                                                                     │
│ → TOTAL PROCESSING TIME: 560ms                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│ FINAL STATE                                                                         │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ Transaction Status: PROCESSED                                                       │
│ Fraud Decision: REJECT                                                              │
│ Fraud Score: 78.25/100                                                             │
│ Alert Created: CRITICAL                                                             │
│ Dashboard Updated: Real-time                                                        │
│ Customer Notified: Pending                                                          │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Detailed Agent Execution Flow

### Parallel Execution Timeline

```
Time (ms)  │ Risk Agent    │ Geo Agent     │ Device Agent  │ AML Agent     │ Behavior Agent
───────────┼───────────────┼───────────────┼───────────────┼───────────────┼────────────────
0          │ START         │ START         │ START         │ START         │ START
           │               │               │               │               │
10         │ Load rules    │ Get country   │ Get device    │ Load          │ Get customer
           │               │ risk data     │ fingerprint   │ watchlists    │ history
           │               │               │               │               │
30         │ Check amount  │ Check         │ Validate      │ Screen        │ Analyze
           │ thresholds    │ location      │ device        │ customer      │ patterns
           │               │               │               │               │
50         │ Calculate     │ Detect        │ Check trust   │ Screen        │ Check
           │ velocity      │ impossible    │ status        │ merchant      │ anomalies
           │               │ travel        │               │               │
70         │ Analyze       │ Calculate     │ Compare       │ Check         │ Calculate
           │ patterns      │ risk score    │ history       │ sanctions     │ deviation
           │               │               │               │               │
90         │ Generate      │ Generate      │ Generate      │ Generate      │ Generate
           │ reasons       │ reasons       │ reasons       │ reasons       │ reasons
           │               │               │               │               │
120        │ COMPLETE      │ COMPLETE      │ COMPLETE      │ COMPLETE      │ COMPLETE
           │ Score: 85.0   │ Score: 90.0   │ Score: 75.0   │ Score: 60.0   │ Score: 80.0
           │ REJECT        │ REJECT        │ REVIEW        │ REVIEW        │ REJECT
```

---

## Stage-by-Stage Breakdown

### Stage 1: Transaction Received (0-50ms)

```
┌─────────────────────────────────────────────────────────────┐
│ API Request                                                 │
│ POST /api/transactions                                      │
│ {                                                           │
│   "txnId": "TXN-002",                                      │
│   "customerId": "CUST-456",                                │
│   "amount": 50000,                                         │
│   "merchant": "Unknown Vendor",                            │
│   "country": "NG",                                         │
│   "deviceId": "unknown-device-123"                         │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ TransactionController.createTransaction()                   │
│ → TransactionService.createTransaction()                    │
│   → Save to PostgreSQL (status=PENDING)                    │
│   → KafkaProducerService.publishTransactionEvent()         │
│     → Publish to "fraud-transactions" topic                │
└─────────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ Response to Client                                          │
│ 201 Created                                                 │
│ {                                                           │
│   "id": 1,                                                 │
│   "txnId": "TXN-002",                                      │
│   "status": "PENDING",                                     │
│   "fraudDecision": null                                    │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
```

### Stage 2: Kafka Consumer Pickup (50-100ms)

```
┌─────────────────────────────────────────────────────────────┐
│ Kafka Topic: "fraud-transactions"                          │
│ Consumer Group: fraud-detection-group                       │
└─────────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ KafkaConsumerService.consumeTransactionEvent()              │
│ → Log transaction details                                   │
│ → FraudOrchestratorService.analyzeTransaction()            │
└─────────────────────────────────────────────────────────────┘
```

### Stage 3: Parallel Agent Analysis (100-400ms)

#### Risk Agent (120ms)
```
┌─────────────────────────────────────────────────────────────┐
│ RiskAgent.analyze()                                         │
├─────────────────────────────────────────────────────────────┤
│ Checks:                                                     │
│ ✓ Amount threshold: $50,000 > $10,000 (HIGH)              │
│ ✓ Velocity: 1 transaction in 1 hour (NORMAL)              │
│ ✓ Pattern: Large amount unusual (HIGH)                     │
│                                                             │
│ Result:                                                     │
│ • Score: 85.0                                              │
│ • Decision: REJECT                                         │
│ • Reasons:                                                 │
│   - "High transaction amount"                              │
│   - "Exceeds customer average by 400%"                     │
│ • Confidence: 90%                                          │
│ • Processing Time: 120ms                                   │
└─────────────────────────────────────────────────────────────┘
```

#### Geo Agent (95ms)
```
┌─────────────────────────────────────────────────────────────┐
│ GeoAgent.analyze()                                          │
├─────────────────────────────────────────────────────────────┤
│ Checks:                                                     │
│ ✓ Country risk: Nigeria (HIGH RISK)                        │
│ ✓ Location history: Previous = USA, Current = NG           │
│ ✓ Impossible travel: Last txn 2 hours ago in USA          │
│                                                             │
│ Result:                                                     │
│ • Score: 90.0                                              │
│ • Decision: REJECT                                         │
│ • Reasons:                                                 │
│   - "High-risk country (Nigeria)"                          │
│   - "Impossible travel detected"                           │
│ • Confidence: 95%                                          │
│ • Processing Time: 95ms                                    │
└─────────────────────────────────────────────────────────────┘
```

#### Device Agent (110ms)
```
┌─────────────────────────────────────────────────────────────┐
│ DeviceAgent.analyze()                                       │
├─────────────────────────────────────────────────────────────┤
│ Checks:                                                     │
│ ✓ Device fingerprint: unknown-device-123                   │
│ ✓ Trusted devices: 2 known devices                         │
│ ✓ Device history: No previous transactions                 │
│                                                             │
│ Result:                                                     │
│ • Score: 75.0                                              │
│ • Decision: REVIEW                                         │
│ • Reasons:                                                 │
│   - "Unknown device fingerprint"                           │
│   - "No previous transactions from this device"            │
│ • Confidence: 85%                                          │
│ • Processing Time: 110ms                                   │
└─────────────────────────────────────────────────────────────┘
```

#### AML Agent (150ms)
```
┌─────────────────────────────────────────────────────────────┐
│ AMLAgent.analyze()                                          │
├─────────────────────────────────────────────────────────────┤
│ Checks:                                                     │
│ ✓ Customer watchlist: Not found                            │
│ ✓ Merchant watchlist: Not found                            │
│ ✓ Sanctions list: Not found                                │
│ ✓ PEP list: Not found                                      │
│                                                             │
│ Result:                                                     │
│ • Score: 60.0                                              │
│ • Decision: REVIEW                                         │
│ • Reasons:                                                 │
│   - "Merchant not on watchlist"                            │
│   - "No suspicious patterns detected"                      │
│ • Confidence: 70%                                          │
│ • Processing Time: 150ms                                   │
└─────────────────────────────────────────────────────────────┘
```

#### Behavior Agent (105ms)
```
┌─────────────────────────────────────────────────────────────┐
│ BehaviorAgent.analyze()                                     │
├─────────────────────────────────────────────────────────────┤
│ Checks:                                                     │
│ ✓ Transaction time: 3:00 AM (UNUSUAL)                      │
│ ✓ Typical time window: 9:00 AM - 6:00 PM                  │
│ ✓ Merchant category: Unknown (DEVIATION)                   │
│ ✓ Spending pattern: Significant deviation                  │
│                                                             │
│ Result:                                                     │
│ • Score: 80.0                                              │
│ • Decision: REJECT                                         │
│ • Reasons:                                                 │
│   - "Unusual transaction time (3:00 AM)"                   │
│   - "Merchant category deviation"                          │
│ • Confidence: 88%                                          │
│ • Processing Time: 105ms                                   │
└─────────────────────────────────────────────────────────────┘
```

### Stage 4: Result Aggregation (450ms)

```
┌─────────────────────────────────────────────────────────────┐
│ FraudOrchestratorService.aggregateResults()                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Weighted Score Calculation:                                │
│ ═══════════════════════════════════════                    │
│                                                             │
│ Agent          Score    Weight    Weighted                 │
│ ─────────────  ─────    ──────    ────────                │
│ Risk Agent     85.0  ×  0.25   =  21.25                   │
│ Geo Agent      90.0  ×  0.20   =  18.00                   │
│ Device Agent   75.0  ×  0.20   =  15.00                   │
│ AML Agent      60.0  ×  0.20   =  12.00                   │
│ Behavior Agent 80.0  ×  0.15   =  12.00                   │
│                                   ──────                    │
│ FINAL SCORE:                      78.25                    │
│                                                             │
│ Agent Consensus:                                           │
│ ═══════════════                                            │
│ REJECT:  3 votes (Risk, Geo, Behavior)                    │
│ REVIEW:  2 votes (Device, AML)                            │
│ APPROVE: 0 votes                                           │
│                                                             │
│ Average Confidence: 85.6%                                  │
└─────────────────────────────────────────────────────────────┘
```

### Stage 5: Decision Making (480ms)

```
┌─────────────────────────────────────────────────────────────┐
│ DecisionService.makeDecision()                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Decision Logic:                                            │
│ ═══════════════                                            │
│                                                             │
│ IF finalScore >= 70:                                       │
│    → REJECT ✓ (Score: 78.25)                              │
│                                                             │
│ IF rejectVotes >= 3:                                       │
│    → REJECT ✓ (Votes: 3)                                  │
│                                                             │
│ FINAL DECISION: REJECT                                     │
│ CONFIDENCE: 92%                                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Performance Metrics

```
┌─────────────────────────────────────────────────────────────┐
│ PERFORMANCE BREAKDOWN                                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Stage                          Time (ms)    % of Total     │
│ ─────────────────────────────  ─────────    ──────────     │
│ 1. Transaction Received        50           8.9%           │
│ 2. Kafka Consumer Pickup       50           8.9%           │
│ 3. Parallel Agent Analysis     300          53.6%          │
│ 4. Result Aggregation          50           8.9%           │
│ 5. Decision Making             30           5.4%           │
│ 6. Explainability Generation   20           3.6%           │
│ 7. Database Update             20           3.6%           │
│ 8. Alert Creation              20           3.6%           │
│ 9. Kafka Acknowledgment        20           3.6%           │
│                                ─────         ─────          │
│ TOTAL:                         560ms        100%           │
│                                                             │
│ Target: < 1000ms ✓                                         │
│ Actual: 560ms                                              │
│ Performance: EXCELLENT                                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## React Component for Timeline Visualization

```javascript
import React from 'react';
import { Timeline, TimelineItem } from '@carbon/react';
import { Checkmark, Warning, InProgress } from '@carbon/icons-react';

const FraudInvestigationTimeline = ({ transaction }) => {
  const stages = [
    {
      title: 'Transaction Received',
      time: '0ms',
      status: 'complete',
      details: 'Saved to database and published to Kafka'
    },
    {
      title: 'Kafka Consumer Pickup',
      time: '50ms',
      status: 'complete',
      details: 'Event consumed and orchestrator triggered'
    },
    {
      title: 'Parallel Agent Analysis',
      time: '100-400ms',
      status: 'complete',
      details: '5 agents executed in parallel',
      agents: [
        { name: 'Risk Agent', score: 85.0, decision: 'REJECT', time: '120ms' },
        { name: 'Geo Agent', score: 90.0, decision: 'REJECT', time: '95ms' },
        { name: 'Device Agent', score: 75.0, decision: 'REVIEW', time: '110ms' },
        { name: 'AML Agent', score: 60.0, decision: 'REVIEW', time: '150ms' },
        { name: 'Behavior Agent', score: 80.0, decision: 'REJECT', time: '105ms' }
      ]
    },
    {
      title: 'Result Aggregation',
      time: '450ms',
      status: 'complete',
      details: 'Weighted score: 78.25, Consensus: REJECT'
    },
    {
      title: 'Decision Making',
      time: '480ms',
      status: 'complete',
      details: 'Final decision: REJECT (Confidence: 92%)'
    },
    {
      title: 'Explainability Generation',
      time: '500ms',
      status: 'complete',
      details: 'AI explanation and risk factors generated'
    },
    {
      title: 'Database Update',
      time: '520ms',
      status: 'complete',
      details: 'Transaction status updated to PROCESSED'
    },
    {
      title: 'Alert Creation',
      time: '540ms',
      status: 'complete',
      details: 'CRITICAL alert created and stored'
    },
    {
      title: 'Processing Complete',
      time: '560ms',
      status: 'complete',
      details: 'Total processing time: 560ms'
    }
  ];

  return (
    <div className="fraud-timeline">
      <h3>Investigation Timeline: {transaction.txnId}</h3>
      <Timeline>
        {stages.map((stage, index) => (
          <TimelineItem
            key={index}
            status={stage.status}
            label={stage.time}
          >
            <h4>{stage.title}</h4>
            <p>{stage.details}</p>
            {stage.agents && (
              <div className="agent-details">
                {stage.agents.map((agent, i) => (
                  <div key={i} className="agent-item">
                    <span>{agent.name}</span>
                    <span>Score: {agent.score}</span>
                    <span>{agent.decision}</span>
                    <span>{agent.time}</span>
                  </div>
                ))}
              </div>
            )}
          </TimelineItem>
        ))}
      </Timeline>
    </div>
  );
};

export default FraudInvestigationTimeline;
```

---

**Made with ❤️ for Fraud Investigation Platform**