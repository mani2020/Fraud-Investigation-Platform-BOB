# High-Risk Transaction End-to-End Flow - Technical Explanation

## Flow Diagram

![Fraud Detection Flow](fraud-flow-diagram.png)

## Complete Flow Summary

**API Entry** → **Database Storage** → **Kafka Publishing** → **Async Processing** → **Agent Analysis** → **Decision Making** → **Final Storage**

---

## Detailed Technical Flow

### 1. API Entry Point
- **Endpoint**: `POST /api/transactions`
- **Controller**: [`TransactionController.createTransaction()`](../../src/main/java/com/fraud/platform/controller/TransactionController.java:34)
- **Request Body**: `TransactionRequest` (txnId, customerId, amount, merchant, country, deviceId, paymentType)
- **Immediate Response**: `201 Created` with Transaction object (status=PENDING)

### 2. Transaction Creation & Initial Storage
- **Service**: [`TransactionService.createTransaction()`](../../src/main/java/com/fraud/platform/service/TransactionService.java:32)
- **Actions**:
  - Validates transaction doesn't exist
  - Creates `Transaction` entity with `status="PENDING"`
  - Saves to **PostgreSQL** via `TransactionRepository`
- **Storage Location**: `transactions` table in PostgreSQL
- **Response**: Returns immediately to client (async processing begins)

### 3. Kafka Event Publishing

#### Topic Configuration
- **Topic Name**: `"fraud-transactions"`
- **Configuration**: Defined in [`application.yml`](../../src/main/resources/application.yml)
- **Producer**: [`KafkaProducerService.publishTransactionEvent()`](../../src/main/java/com/fraud/platform/kafka/KafkaProducerService.java:31)
- **Event Type**: [`TransactionEvent`](../../src/main/java/com/fraud/platform/kafka/events/TransactionEvent.java)

#### Publishing Process
```java
// Event published to Kafka topic
Topic: "fraud-transactions"
Key: transaction.txnId
Value: TransactionEvent (JSON serialized)
Acknowledgment: Async with callback
```

### 4. Kafka Consumer Processing
- **Consumer**: [`KafkaConsumerService.consumeTransactionEvent()`](../../src/main/java/com/fraud/platform/kafka/KafkaConsumerService.java:40)
- **Consumer Group**: Configured in application.yml
- **Acknowledgment**: Manual acknowledgment after successful processing
- **Error Handling**: Failed messages are not acknowledged and will be reprocessed

### 5. Fraud Orchestration

#### Orchestrator Service
- **Service**: [`FraudOrchestratorService.analyzeTransaction()`](../../src/main/java/com/fraud/platform/orchestrator/FraudOrchestratorService.java:53)
- **Execution Model**: Parallel agent execution using `ExecutorService` (5-thread pool)
- **Processing Time**: Tracked and logged in `FraudDecision.totalProcessingTimeMs`

### 6. Agent Involvement (Parallel Execution)

All agents implement [`FraudAgent`](../../src/main/java/com/fraud/platform/agents/FraudAgent.java) interface and execute simultaneously:

#### Agent Details

| Agent | Weight | Responsibility | Key Checks |
|-------|--------|----------------|------------|
| **RiskAgent** | 0.25 | Transaction risk analysis | Amount thresholds, velocity patterns, transaction frequency |
| **GeoAgent** | 0.20 | Geographic risk detection | Country risk levels, impossible travel, location anomalies |
| **DeviceAgent** | 0.20 | Device fingerprint validation | Trusted device status, device history, fingerprint matching |
| **AMLAgent** | 0.20 | Anti-Money Laundering | Watchlist screening, suspicious patterns, regulatory compliance |
| **BehaviorAgent** | 0.15 | Customer behavior patterns | Historical behavior, spending patterns, anomaly detection |

#### Agent Output
Each agent returns an [`AgentResult`](../../src/main/java/com/fraud/platform/model/AgentResult.java) containing:
- `riskScore`: 0-100 (higher = more risky)
- `decision`: "APPROVE", "REVIEW", or "REJECT"
- `reasons`: List of specific risk factors identified
- `confidence`: Agent's confidence level (0-100)
- `processingTimeMs`: Time taken for analysis

### 7. Result Aggregation

#### Aggregation Logic
- **Method**: [`FraudOrchestratorService.aggregateResults()`](../../src/main/java/com/fraud/platform/orchestrator/FraudOrchestratorService.java:134)
- **Calculation**:
  ```
  finalScore = Σ(agentScore × agentWeight) / Σ(agentWeight)
  avgConfidence = Σ(agentConfidence) / numberOfAgents
  ```
- **Consensus Analysis**: Counts APPROVE/REVIEW/REJECT decisions across agents
- **Output**: [`FraudDecision`](../../src/main/java/com/fraud/platform/model/FraudDecision.java) object

### 8. Final Decision Making

#### Decision Service
- **Service**: [`DecisionService.makeDecision()`](../../src/main/java/com/fraud/platform/service/DecisionService.java)
- **Decision Logic**:
  ```
  if (finalScore >= 70 OR rejectCount >= 3) → REJECT
  else if (finalScore >= 40 OR reviewCount > 0) → REVIEW
  else → APPROVE
  ```
- **Special Rules**:
  - Any agent REJECT + score ≥ 60 → REJECT
  - Majority (3+) agents say REJECT → REJECT
  - Score ≥ 70 → REJECT (regardless of agent consensus)

### 9. Explainability Generation

#### Explainability Service
- **Service**: [`ExplainabilityService`](../../src/main/java/com/fraud/platform/service/ExplainabilityService.java)
- **Generates**:
  - **Human-readable explanation**: Detailed breakdown of decision factors
  - **Short summary**: One-line summary for quick review
  - **Risk factor breakdown**: Categorized risk factors with scores
  - **Agent contributions**: Individual agent impacts on final decision

#### Example Output
```
Decision: REJECT (Score: 75.5)
Summary: High-risk transaction flagged by multiple agents

Risk Factors:
- High transaction amount (RiskAgent: 85)
- Suspicious country (GeoAgent: 70)
- Unknown device (DeviceAgent: 65)
- Velocity threshold exceeded (BehaviorAgent: 80)
```

### 10. Transaction Update & Final Storage

#### Update Process
- **Method**: [`TransactionService.updateFraudDecision()`](../../src/main/java/com/fraud/platform/service/TransactionService.java:117)
- **Updates**:
  - `fraudDecision`: "APPROVE" / "REVIEW" / "REJECT"
  - `fraudScore`: Final weighted score (BigDecimal)
  - `status`: "PROCESSED"
- **Storage**: Updated in PostgreSQL `transactions` table
- **Audit**: Logged in `fraud_audit_logs` table

---

## Data Storage Locations

### 1. Initial Transaction Storage
- **Table**: `transactions`
- **Database**: PostgreSQL
- **Status**: `PENDING`
- **Timing**: Synchronous (before API response)

### 2. Kafka Event Stream
- **Topic**: `fraud-transactions`
- **Retention**: Configurable (default: 7 days)
- **Purpose**: Async processing queue
- **Cleanup**: Automatic based on retention policy

### 3. Final Decision Storage
- **Table**: `transactions` (updated)
- **Database**: PostgreSQL
- **Status**: `PROCESSED`
- **Fields**: `fraudDecision`, `fraudScore`, `status`
- **Timing**: After fraud analysis completion

### 4. Audit Trail
- **Table**: `fraud_audit_logs`
- **Database**: PostgreSQL
- **Purpose**: Complete audit trail of all fraud decisions
- **Retention**: Permanent (for compliance)

---

## Performance Characteristics

### Throughput
- **API Response Time**: < 100ms (synchronous part only)
- **Fraud Analysis Time**: 200-500ms (parallel agent execution)
- **Total Processing Time**: < 1 second end-to-end

### Scalability
- **Kafka Partitions**: Horizontal scaling for high throughput
- **Consumer Groups**: Multiple consumers for parallel processing
- **Thread Pool**: 5 threads for parallel agent execution
- **Database**: Connection pooling for optimal performance

### Reliability
- **Kafka Acknowledgment**: Manual ack ensures no message loss
- **Error Handling**: Failed messages reprocessed automatically
- **Database Transactions**: ACID compliance for data integrity
- **Audit Trail**: Complete traceability of all decisions

---

## API Endpoints for Retrieving Results

### Get Transaction by ID
```http
GET /api/transactions/{txnId}
```
Returns complete transaction with fraud decision.

### Get Customer Transactions
```http
GET /api/transactions/customer/{customerId}
```
Returns all transactions for a customer, ordered by timestamp.

### Get All Transactions
```http
GET /api/transactions
```
Returns all transactions in the system.

---

## Example High-Risk Transaction Flow

### Request
```json
POST /api/transactions
{
  "txnId": "TXN-2026-001",
  "customerId": "CUST-12345",
  "amount": 50000.00,
  "merchant": "Unknown Merchant",
  "country": "NG",
  "deviceId": "unknown-device-123",
  "paymentType": "WIRE_TRANSFER"
}
```

### Immediate Response (< 100ms)
```json
{
  "id": 1,
  "txnId": "TXN-2026-001",
  "customerId": "CUST-12345",
  "amount": 50000.00,
  "status": "PENDING",
  "fraudDecision": null,
  "fraudScore": null
}
```

### Async Processing (200-500ms)
1. Kafka event published to `fraud-transactions`
2. Consumer picks up event
3. 5 agents analyze in parallel:
   - RiskAgent: Score 85 (REJECT) - High amount
   - GeoAgent: Score 90 (REJECT) - High-risk country
   - DeviceAgent: Score 75 (REJECT) - Unknown device
   - AMLAgent: Score 60 (REVIEW) - No watchlist match
   - BehaviorAgent: Score 80 (REJECT) - Unusual pattern
4. Aggregation: Final Score = 78.5
5. Decision: REJECT

### Final State (Query after processing)
```json
GET /api/transactions/TXN-2026-001
{
  "id": 1,
  "txnId": "TXN-2026-001",
  "customerId": "CUST-12345",
  "amount": 50000.00,
  "status": "PROCESSED",
  "fraudDecision": "REJECT",
  "fraudScore": 78.5
}
```

---

## Monitoring & Observability

### Logging
- **Framework**: Logback with structured logging
- **Levels**: INFO for flow, DEBUG for details, ERROR for failures
- **Key Events**:
  - Transaction received
  - Kafka event published/consumed
  - Agent execution start/complete
  - Final decision made
  - Database updates

### Metrics (Potential)
- Transaction processing rate
- Agent execution times
- Kafka lag
- Decision distribution (APPROVE/REVIEW/REJECT)
- Error rates

---

## Security Considerations

### Data Protection
- Sensitive data encrypted at rest (database)
- TLS for Kafka communication
- API authentication/authorization (to be implemented)

### Audit Compliance
- Complete audit trail in `fraud_audit_logs`
- Immutable decision records
- Timestamp tracking for all events

---

## Related Documentation

- [Architecture Overview](../architecture/ARCHITECTURE.md)
- [Database Schema](DATABASE_SCHEMA.md)
- [Decision Service Output](DECISION_SERVICE_SAMPLE_OUTPUT.md)
- [Explainability Output](EXPLAINABILITY_SAMPLE_OUTPUT.md)
- [Setup & Testing Guide](../guides/SETUP_TESTING.md)