# Fraud Notification Service Documentation

## Overview

The Fraud Notification Service provides real-time alerts for dashboard and fraud investigation purposes. It automatically creates alerts when fraud is detected and provides APIs for managing and querying these alerts.

## Architecture

### Components

1. **FraudAlert Model** - Alert data structure
2. **FraudNotificationService** - Core notification logic
3. **FraudAlertController** - REST API endpoints
4. **Integration** - Automatic alert creation in FraudOrchestratorService

## Alert Model

### FraudAlert Structure

```java
{
  "alertId": "ALERT-1234567890-123",
  "severity": "CRITICAL",
  "alertType": "FRAUD_DETECTED",
  "txnId": "TXN1001",
  "customerId": "CUST-12345",
  "message": "Fraud detected - Score: 85.5",
  "detailedMessage": "Transaction TXN1001 flagged with fraud score 85.5...",
  "fraudScore": 85.5,
  "decision": "REJECT",
  "timestamp": "2026-05-11T14:30:00",
  "acknowledged": false,
  "acknowledgedBy": null,
  "acknowledgedAt": null
}
```

### Severity Levels

| Severity | Score Range | Decision | Description |
|----------|-------------|----------|-------------|
| **CRITICAL** | ≥ 70 | REJECT | High-risk fraud detected, immediate action required |
| **HIGH** | 50-69 | REVIEW | Moderate-risk requiring manual review |
| **MEDIUM** | 30-49 | REVIEW | Low-risk but flagged for investigation |
| **LOW** | < 30 | APPROVE | Informational, minimal risk |

### Alert Types

- **FRAUD_DETECTED** - Confirmed fraud pattern
- **SUSPICIOUS_ACTIVITY** - Unusual activity requiring review
- **VELOCITY_EXCEEDED** - Transaction velocity threshold exceeded
- **HIGH_RISK_COUNTRY** - Transaction from high-risk country
- **UNKNOWN_DEVICE** - Transaction from unrecognized device
- **AML_WATCHLIST_MATCH** - Customer/merchant on AML watchlist
- **BEHAVIOR_ANOMALY** - Unusual customer behavior detected

## API Endpoints

### Base URL
```
http://localhost:8080/api/alerts
```

### 1. Get All Recent Alerts

```http
GET /api/alerts
```

**Response:**
```json
[
  {
    "alertId": "ALERT-1234567890-123",
    "severity": "CRITICAL",
    "txnId": "TXN1001",
    "message": "Fraud detected - Score: 85.5",
    "fraudScore": 85.5,
    "timestamp": "2026-05-11T14:30:00",
    "acknowledged": false
  }
]
```

### 2. Get Alerts by Severity

```http
GET /api/alerts/severity/{severity}
```

**Parameters:**
- `severity`: CRITICAL, HIGH, MEDIUM, or LOW

**Example:**
```http
GET /api/alerts/severity/CRITICAL
```

### 3. Get Critical Alerts Only

```http
GET /api/alerts/critical
```

Returns only CRITICAL severity alerts.

### 4. Get Unacknowledged Alerts

```http
GET /api/alerts/unacknowledged
```

Returns all alerts that haven't been acknowledged by analysts.

### 5. Get Alert by ID

```http
GET /api/alerts/{alertId}
```

**Example:**
```http
GET /api/alerts/ALERT-1234567890-123
```

### 6. Acknowledge Alert

```http
PUT /api/alerts/{alertId}/acknowledge
```

**Request Body:**
```json
{
  "acknowledgedBy": "analyst@example.com"
}
```

**Response:**
```json
{
  "alertId": "ALERT-1234567890-123",
  "acknowledged": true,
  "acknowledgedBy": "analyst@example.com",
  "acknowledgedAt": "2026-05-11T14:35:00"
}
```

### 7. Get Alert Statistics

```http
GET /api/alerts/stats
```

**Response:**
```json
{
  "CRITICAL": 15,
  "HIGH": 42,
  "MEDIUM": 28,
  "LOW": 5
}
```

### 8. Health Check

```http
GET /api/alerts/health
```

## Integration Flow

### Automatic Alert Creation

Alerts are automatically created during fraud detection:

```
1. Transaction submitted → POST /api/transactions
2. Fraud analysis performed → FraudOrchestratorService
3. Decision made → APPROVE/REVIEW/REJECT
4. Alert created (if REVIEW or REJECT) → FraudNotificationService
5. Alert available via API → GET /api/alerts
```

### Alert Creation Logic

```java
// In FraudOrchestratorService.analyzeTransaction()
if ("REVIEW".equals(decision) || "REJECT".equals(decision)) {
    FraudAlert alert = notificationService.createAlert(decision);
}
```

## Usage Examples

### Example 1: Dashboard Polling for Critical Alerts

```javascript
// Frontend dashboard code
async function fetchCriticalAlerts() {
  const response = await fetch('http://localhost:8080/api/alerts/critical');
  const alerts = await response.json();
  
  // Display alerts in dashboard
  alerts.forEach(alert => {
    displayAlert(alert);
  });
}

// Poll every 5 seconds
setInterval(fetchCriticalAlerts, 5000);
```

### Example 2: Acknowledge Alert

```javascript
async function acknowledgeAlert(alertId, analyst) {
  const response = await fetch(
    `http://localhost:8080/api/alerts/${alertId}/acknowledge`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ acknowledgedBy: analyst })
    }
  );
  
  return await response.json();
}
```

### Example 3: Get Unacknowledged Count

```javascript
async function getUnacknowledgedCount() {
  const response = await fetch('http://localhost:8080/api/alerts/unacknowledged');
  const alerts = await response.json();
  return alerts.length;
}
```

## Alert Message Examples

### Critical Alert (Score ≥ 70)

```json
{
  "severity": "CRITICAL",
  "txnId": "TXN1001",
  "message": "Fraud detected - Score: 85.5",
  "detailedMessage": "Transaction TXN1001 flagged with fraud score 85.5. Decision: REJECT. Reasons: High transaction amount, Suspicious country, Unknown device"
}
```

### High Alert (Score 50-69)

```json
{
  "severity": "HIGH",
  "txnId": "TXN1002",
  "message": "Suspicious activity - Score: 62.3",
  "detailedMessage": "Transaction TXN1002 flagged with fraud score 62.3. Decision: REVIEW. Reasons: Velocity threshold exceeded, New merchant"
}
```

### Medium Alert (Score 30-49)

```json
{
  "severity": "MEDIUM",
  "txnId": "TXN1003",
  "message": "Transaction flagged - Score: 38.7",
  "detailedMessage": "Transaction TXN1003 flagged with fraud score 38.7. Decision: REVIEW. Reasons: Unusual time of day"
}
```

## Storage

### In-Memory Storage
- Stores last **1000 alerts** in memory
- Thread-safe using `CopyOnWriteArrayList` and `ConcurrentHashMap`
- Oldest alerts automatically removed when limit reached

### Future Enhancements
- Persist alerts to database (fraud_alerts table)
- Add alert history and audit trail
- Implement alert expiration policies
- Add alert notification channels (email, SMS, Slack)

## Performance Considerations

### Memory Usage
- Each alert: ~500 bytes
- 1000 alerts: ~500 KB
- Negligible impact on application memory

### Thread Safety
- All operations are thread-safe
- Concurrent read/write operations supported
- No locking required for reads

### Scalability
- Current implementation: Single instance
- For distributed systems: Use Redis or database
- Consider event streaming for real-time notifications

## Testing

### Manual Testing

```bash
# 1. Submit a high-risk transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "txnId": "TXN-TEST-001",
    "customerId": "CUST-12345",
    "amount": 50000,
    "merchant": "Unknown Merchant",
    "country": "NG",
    "deviceId": "unknown-device",
    "paymentType": "WIRE_TRANSFER"
  }'

# 2. Wait for processing (1-2 seconds)

# 3. Check for alerts
curl http://localhost:8080/api/alerts/critical

# 4. Acknowledge alert
curl -X PUT http://localhost:8080/api/alerts/ALERT-XXX/acknowledge \
  -H "Content-Type: application/json" \
  -d '{"acknowledgedBy": "test-analyst"}'
```

### Expected Behavior

1. High-risk transaction creates CRITICAL alert
2. Alert appears in `/api/alerts/critical`
3. Alert appears in `/api/alerts/unacknowledged`
4. After acknowledgment, alert removed from unacknowledged list
5. Alert statistics updated

## Monitoring

### Key Metrics to Track

- Total alerts created
- Alerts by severity
- Average acknowledgment time
- Unacknowledged alert count
- Alert creation rate

### Logging

All alert operations are logged:

```
INFO  - Creating fraud alert for transaction: TXN1001
INFO  - Fraud alert created: alertId=ALERT-123, severity=CRITICAL, txnId=TXN1001
INFO  - Alert acknowledged: alertId=ALERT-123, by=analyst@example.com
```

## Related Documentation

- [Fraud Flow Technical](FRAUD_FLOW_TECHNICAL.md) - Complete transaction flow
- [Decision Service Output](DECISION_SERVICE_SAMPLE_OUTPUT.md) - Fraud decision examples
- [Explainability Output](EXPLAINABILITY_SAMPLE_OUTPUT.md) - AI explanation examples

---

**Made with Bob**