# DecisionService Sample Output

This document shows sample outputs from the DecisionService with the 4-tier decision system: APPROVE, OTP, HOLD, BLOCK.

---

## Decision Thresholds

| Decision | Score Range | Description |
|----------|-------------|-------------|
| **APPROVE** | < 30 | Low risk - Process normally |
| **OTP** | 30 - 69 | Medium risk - Require additional authentication |
| **HOLD** | 70 - 84 | High risk - Manual review required |
| **BLOCK** | ≥ 85 | Critical risk - Block immediately |

---

## Scenario 1: BLOCK Decision (Score: 90)

### Input Transaction:
```json
{
  "txnId": "TXN1001",
  "customerId": "C100",
  "amount": 150000,
  "merchant": "CRYPTO_STORE",
  "country": "Russia",
  "deviceId": "UNKNOWN_DEVICE",
  "paymentType": "UPI"
}
```

### Decision Matrix (Console Output):

```
╔════════════════════════════════════════╗
║     FRAUD DECISION MATRIX              ║
╠════════════════════════════════════════╣
║ Transaction ID: TXN1001                ║
╠════════════════════════════════════════╣
║ Agent Scores:                          ║
╠════════════════════════════════════════╣
║ Risk                  90 █████████·   ║
║ Geo                   50 ▒▒▒▒▒·····   ║
║ Device                65 ▓▓▓▓▓▓····   ║
║ AML                   30 ▒▒▒·······   ║
║ Behavior              25 ░░·········   ║
╠════════════════════════════════════════╣
║ TOTAL SCORE           90 █████████·   ║
╠════════════════════════════════════════╣
║ Decision Thresholds:                   ║
║   APPROVE:  < 30                        ║
║   OTP:      30  - 69                    ║
║   HOLD:     70  - 84                    ║
║   BLOCK:    ≥ 85                        ║
╠════════════════════════════════════════╣
║ FINAL DECISION: 🚫 BLOCK               ║
╚════════════════════════════════════════╝
```

### Simple Table (Log Output):

```
┌──────────────────────┬───────┐
│ Agent                │ Score │
├──────────────────────┼───────┤
│ Risk                 │    90 │
│ Geo                  │    50 │
│ Device               │    65 │
│ AML                  │    30 │
│ Behavior             │    25 │
├──────────────────────┼───────┤
│ TOTAL                │    90 │
└──────────────────────┴───────┘

Decision: BLOCK
```

### Decision Explanation:
```
Transaction blocked - Critical fraud risk detected. Do not process. Contact customer immediately.
```

### Action Items:
- Block transaction immediately
- Send alert to customer via SMS/email
- Flag account for enhanced monitoring
- Initiate fraud investigation
- Consider temporary account freeze
- Document all findings for compliance

---

## Scenario 2: HOLD Decision (Score: 75)

### Input Transaction:
```json
{
  "txnId": "TXN2002",
  "customerId": "C200",
  "amount": 95000,
  "merchant": "JEWELRY_STORE",
  "country": "Ukraine",
  "deviceId": "DEV456",
  "paymentType": "CREDIT_CARD"
}
```

### Decision Matrix:

```
╔════════════════════════════════════════╗
║     FRAUD DECISION MATRIX              ║
╠════════════════════════════════════════╣
║ Transaction ID: TXN2002                ║
╠════════════════════════════════════════╣
║ Agent Scores:                          ║
╠════════════════════════════════════════╣
║ Risk                  55 ▒▒▒▒▒·····   ║
║ Geo                   25 ░░········   ║
║ Device                10 ░·········   ║
║ AML                   40 ▒▒▒▒······   ║
║ Behavior              15 ░·········   ║
╠════════════════════════════════════════╣
║ TOTAL SCORE           75 ▓▓▓▓▓▓▓···   ║
╠════════════════════════════════════════╣
║ Decision Thresholds:                   ║
║   APPROVE:  < 30                        ║
║   OTP:      30  - 69                    ║
║   HOLD:     70  - 84                    ║
║   BLOCK:    ≥ 85                        ║
╠════════════════════════════════════════╣
║ FINAL DECISION: ⏸️ HOLD                 ║
╚════════════════════════════════════════╝
```

### Decision Explanation:
```
Transaction on hold - High fraud risk detected. Manual review required before processing.
```

### Action Items:
- Place transaction on hold
- Assign to fraud analyst for manual review
- Contact customer for verification
- Request supporting documentation
- Review within 24 hours

---

## Scenario 3: OTP Decision (Score: 55)

### Input Transaction:
```json
{
  "txnId": "TXN3003",
  "customerId": "C300",
  "amount": 45000,
  "merchant": "ELECTRONICS_STORE",
  "country": "India",
  "deviceId": "NEW_DEVICE_789",
  "paymentType": "UPI"
}
```

### Decision Matrix:

```
╔════════════════════════════════════════╗
║     FRAUD DECISION MATRIX              ║
╠════════════════════════════════════════╣
║ Transaction ID: TXN3003                ║
╠════════════════════════════════════════╣
║ Agent Scores:                          ║
╠════════════════════════════════════════╣
║ Risk                  40 ▒▒▒▒······   ║
║ Geo                    0 ··········   ║
║ Device                30 ▒▒▒·······   ║
║ AML                   10 ░·········   ║
║ Behavior              25 ░░········   ║
╠════════════════════════════════════════╣
║ TOTAL SCORE           55 ▒▒▒▒▒·····   ║
╠════════════════════════════════════════╣
║ Decision Thresholds:                   ║
║   APPROVE:  < 30                        ║
║   OTP:      30  - 69                    ║
║   HOLD:     70  - 84                    ║
║   BLOCK:    ≥ 85                        ║
╠════════════════════════════════════════╣
║ FINAL DECISION: 🔐 OTP                  ║
╚════════════════════════════════════════╝
```

### Decision Explanation:
```
OTP verification required - Medium fraud risk detected. Additional authentication needed.
```

### Action Items:
- Send OTP to registered mobile number
- Wait for customer verification
- Process only after successful OTP validation
- Log OTP attempt for audit trail

---

## Scenario 4: APPROVE Decision (Score: 20)

### Input Transaction:
```json
{
  "txnId": "TXN4004",
  "customerId": "C400",
  "amount": 5000,
  "merchant": "GROCERY_STORE",
  "country": "India",
  "deviceId": "KNOWN_DEVICE_123",
  "paymentType": "UPI"
}
```

### Decision Matrix:

```
╔════════════════════════════════════════╗
║     FRAUD DECISION MATRIX              ║
╠════════════════════════════════════════╣
║ Transaction ID: TXN4004                ║
╠════════════════════════════════════════╣
║ Agent Scores:                          ║
╠════════════════════════════════════════╣
║ Risk                   0 ··········   ║
║ Geo                    0 ··········   ║
║ Device                10 ░·········   ║
║ AML                    0 ··········   ║
║ Behavior              10 ░·········   ║
╠════════════════════════════════════════╣
║ TOTAL SCORE           20 ░░········   ║
╠════════════════════════════════════════╣
║ Decision Thresholds:                   ║
║   APPROVE:  < 30                        ║
║   OTP:      30  - 69                    ║
║   HOLD:     70  - 84                    ║
║   BLOCK:    ≥ 85                        ║
╠════════════════════════════════════════╣
║ FINAL DECISION: ✅ APPROVE              ║
╚════════════════════════════════════════╝
```

### Decision Explanation:
```
Transaction approved - Low fraud risk detected. Process normally.
```

### Action Items:
- Process transaction normally
- Continue standard monitoring
- No additional verification required

---

## Markdown Table Format (For Reports)

### Example Output:

## Fraud Decision Matrix

**Transaction ID:** TXN1001  
**Timestamp:** 2026-05-10T16:30:00  

| Agent | Score | Weight | Weighted Score |
|-------|-------|--------|----------------|
| Risk | 90 | 25% | 22.50 |
| Geo | 50 | 20% | 10.00 |
| Device | 65 | 20% | 13.00 |
| AML | 30 | 25% | 7.50 |
| Behavior | 25 | 10% | 2.50 |
|-------|-------|--------|----------------|
| **TOTAL** | **90** | **100%** | **90.00** |

### Decision Thresholds

- **APPROVE:** < 30
- **OTP:** 30 - 69
- **HOLD:** 70 - 84
- **BLOCK:** ≥ 85

### Final Decision: **BLOCK** 🚫

---

## Score Bar Legend

The visual score bars use different characters to indicate risk levels:

| Character | Risk Level | Score Range |
|-----------|------------|-------------|
| █ | Critical | 85-100 |
| ▓ | High | 70-84 |
| ▒ | Medium | 50-69 |
| ░ | Low | 30-49 |
| · | Minimal | 0-29 |

---

## Integration with Logs

### Sample Log Output:

```
2026-05-10 16:30:15 - INFO - Fraud analysis completed for TXN1001:

┌──────────────────────┬───────┐
│ Agent                │ Score │
├──────────────────────┼───────┤
│ Risk                 │    90 │
│ Geo                  │    50 │
│ Device               │    65 │
│ AML                  │    30 │
│ Behavior             │    25 │
├──────────────────────┼───────┤
│ TOTAL                │    90 │
└──────────────────────┴───────┘

Decision: BLOCK

2026-05-10 16:30:15 - INFO - Summary: Transaction BLOCK - Amount exceeds normal spending pattern (+4 more concerns)
```

---

## API Response Format

### JSON Response with Decision Matrix:

```json
{
  "txnId": "TXN1001",
  "finalScore": 90.00,
  "decision": "BLOCK",
  "confidence": 0.82,
  "timestamp": "2026-05-10T16:30:00",
  "agentResults": [
    {
      "agentName": "RiskAgent",
      "riskScore": 90,
      "decision": "REJECT",
      "weight": 0.25,
      "weightedScore": 22.50
    },
    {
      "agentName": "GeoAgent",
      "riskScore": 50,
      "decision": "REVIEW",
      "weight": 0.20,
      "weightedScore": 10.00
    },
    {
      "agentName": "DeviceAgent",
      "riskScore": 65,
      "decision": "REJECT",
      "weight": 0.20,
      "weightedScore": 13.00
    },
    {
      "agentName": "AMLAgent",
      "riskScore": 30,
      "decision": "APPROVE",
      "weight": 0.25,
      "weightedScore": 7.50
    },
    {
      "agentName": "BehaviorAgent",
      "riskScore": 25,
      "decision": "APPROVE",
      "weight": 0.10,
      "weightedScore": 2.50
    }
  ],
  "decisionExplanation": "Transaction blocked - Critical fraud risk detected. Do not process. Contact customer immediately.",
  "actionItems": [
    "Block transaction immediately",
    "Send alert to customer via SMS/email",
    "Flag account for enhanced monitoring",
    "Initiate fraud investigation",
    "Consider temporary account freeze",
    "Document all findings for compliance"
  ],
  "shortSummary": "Transaction BLOCK - Amount exceeds normal spending pattern (+4 more concerns)"
}
```

---

## Configuration

### application.yml:

```yaml
fraud:
  decision:
    approve-threshold: 30   # < 30 = APPROVE
    otp-threshold: 50       # 30-69 = OTP
    hold-threshold: 70      # 70-84 = HOLD
    block-threshold: 85     # ≥ 85 = BLOCK
```

These thresholds can be adjusted based on:
- Risk appetite
- False positive rates
- Customer experience requirements
- Regulatory requirements

---

## Benefits of 4-Tier Decision System

### 1. **APPROVE (< 30)**
- **Customer Experience:** Seamless, no friction
- **Processing:** Instant approval
- **Use Case:** Regular, low-risk transactions

### 2. **OTP (30-69)**
- **Customer Experience:** Minor friction, quick verification
- **Processing:** 30-60 second delay for OTP
- **Use Case:** Slightly elevated risk, new device, unusual amount
- **Benefit:** Balances security with convenience

### 3. **HOLD (70-84)**
- **Customer Experience:** Transaction pending, customer contacted
- **Processing:** Manual review within 24 hours
- **Use Case:** High-risk indicators, requires investigation
- **Benefit:** Prevents fraud while allowing legitimate transactions after verification

### 4. **BLOCK (≥ 85)**
- **Customer Experience:** Transaction declined, immediate alert
- **Processing:** Instant block, fraud team notified
- **Use Case:** Critical fraud indicators, clear threat
- **Benefit:** Prevents fraud losses, protects customer account

---

*Generated by DecisionService - Fraud Investigation Platform v1.0.0*