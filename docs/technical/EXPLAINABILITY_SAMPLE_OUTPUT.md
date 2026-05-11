# ExplainabilityService Sample Output

This document shows sample outputs from the ExplainabilityService for different fraud scenarios.

---

## Scenario 1: HIGH RISK - Transaction REJECTED

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

### Human-Readable Explanation:

```
🚫 TRANSACTION REJECT
Transaction ID: TXN1001
Risk Score: 85.50/100
Confidence: 82%

RISK SUMMARY:
Transaction reject because:
• Amount exceeds normal spending pattern
• Payment originated from high-risk country
• Suspicious device characteristics detected (emulator, VPN, or rooted device)
• Transaction with high-risk merchant category (crypto, gambling, forex)
• Transaction from previously unknown device

DETAILED ANALYSIS:

📊 Transaction Risk Analysis: CRITICAL Risk (Score: 90)
   • Amount exceeds normal spending pattern
   • Transaction with high-risk merchant category (crypto, gambling, forex)

📊 Geographic Location Analysis: CRITICAL Risk (Score: 50)
   • Payment originated from high-risk country

📊 Device Fingerprint Analysis: HIGH Risk (Score: 65)
   • Suspicious device characteristics detected (emulator, VPN, or rooted device)
   • Transaction from previously unknown device

📊 Anti-Money Laundering Analysis: MEDIUM Risk (Score: 30)
   • Round amount transaction (common in money laundering)

📊 Behavioral Pattern Analysis: LOW Risk (Score: 25)
   • First transaction for this customer (no historical pattern)

RECOMMENDED ACTIONS:
• BLOCK transaction immediately
• Contact customer to verify transaction legitimacy
• Flag account for enhanced monitoring
• Consider temporary account restrictions
• Document all findings for compliance review
```

### Short Summary:
```
Transaction REJECT - Amount exceeds normal spending pattern (+4 more concerns)
```

---

## Scenario 2: MEDIUM RISK - Transaction REVIEW

### Input Transaction:
```json
{
  "txnId": "TXN2002",
  "customerId": "C200",
  "amount": 75000,
  "merchant": "ELECTRONICS_STORE",
  "country": "India",
  "deviceId": "DEV456",
  "paymentType": "CREDIT_CARD",
  "timestamp": "2026-05-10T03:30:00"
}
```

### Human-Readable Explanation:

```
⚠️ TRANSACTION REVIEW
Transaction ID: TXN2002
Risk Score: 52.30/100
Confidence: 78%

RISK SUMMARY:
Transaction review because:
• Transaction amount moderately higher than usual
• Transaction during high-risk hours (2 AM - 5 AM)
• Transaction during unusual hours (late night/early morning)

DETAILED ANALYSIS:

📊 Transaction Risk Analysis: HIGH Risk (Score: 55)
   • Transaction amount moderately higher than usual

📊 Geographic Location Analysis: MINIMAL Risk (Score: 0)
   • No concerns identified

📊 Device Fingerprint Analysis: MINIMAL Risk (Score: 0)
   • No concerns identified

📊 Anti-Money Laundering Analysis: LOW Risk (Score: 10)
   • Round amount transaction (common in money laundering)

📊 Behavioral Pattern Analysis: MEDIUM Risk (Score: 35)
   • Transaction during high-risk hours (2 AM - 5 AM)
   • Transaction during unusual hours (late night/early morning)

RECOMMENDED ACTIONS:
• HOLD transaction for manual review
• Contact customer for additional verification
• Request supporting documentation if needed
• Review customer's recent transaction history
• Escalate to senior fraud analyst if concerns persist
```

### Short Summary:
```
Transaction REVIEW - Transaction amount moderately higher than usual (+2 more concerns)
```

---

## Scenario 3: LOW RISK - Transaction APPROVED

### Input Transaction:
```json
{
  "txnId": "TXN3003",
  "customerId": "C300",
  "amount": 5000,
  "merchant": "GROCERY_STORE",
  "country": "India",
  "deviceId": "DEV789",
  "paymentType": "UPI",
  "timestamp": "2026-05-10T14:30:00"
}
```

### Human-Readable Explanation:

```
✅ TRANSACTION APPROVE
Transaction ID: TXN3003
Risk Score: 15.20/100
Confidence: 85%

RISK SUMMARY:
• No significant fraud indicators detected

DETAILED ANALYSIS:

📊 Transaction Risk Analysis: MINIMAL Risk (Score: 0)
   • No concerns identified

📊 Geographic Location Analysis: MINIMAL Risk (Score: 0)
   • No concerns identified

📊 Device Fingerprint Analysis: LOW Risk (Score: 10)
   • Device used by 3 customers

📊 Anti-Money Laundering Analysis: MINIMAL Risk (Score: 0)
   • No concerns identified

📊 Behavioral Pattern Analysis: LOW Risk (Score: 10)
   • Weekend transaction

RECOMMENDED ACTIONS:
• APPROVE transaction for processing
• Continue standard monitoring
• No immediate action required
```

### Short Summary:
```
Transaction APPROVE - No significant fraud indicators
```

---

## Scenario 4: CRITICAL RISK - Multiple Red Flags

### Input Transaction:
```json
{
  "txnId": "TXN4004",
  "customerId": "C400",
  "amount": 9800,
  "merchant": "MONEY_TRANSFER",
  "country": "Nigeria",
  "deviceId": "EMULATOR_001",
  "paymentType": "WIRE_TRANSFER",
  "timestamp": "2026-05-10T03:00:00"
}
```

### Human-Readable Explanation:

```
🚫 TRANSACTION REJECT
Transaction ID: TXN4004
Risk Score: 92.75/100
Confidence: 88%

RISK SUMMARY:
Transaction reject because:
• Payment originated from high-risk country
• Suspicious device characteristics detected (emulator, VPN, or rooted device)
• High-risk payment method used (crypto, wire transfer, international)
• Merchant category associated with money laundering risk
• Potential structuring detected (amount just below reporting threshold)

DETAILED ANALYSIS:

📊 Transaction Risk Analysis: CRITICAL Risk (Score: 70)
   • High-risk payment method used (crypto, wire transfer, international)

📊 Geographic Location Analysis: CRITICAL Risk (Score: 50)
   • Payment originated from high-risk country

📊 Device Fingerprint Analysis: CRITICAL Risk (Score: 35)
   • Suspicious device characteristics detected (emulator, VPN, or rooted device)

📊 Anti-Money Laundering Analysis: CRITICAL Risk (Score: 70)
   • Potential structuring detected (amount just below reporting threshold)
   • Merchant category associated with money laundering risk

📊 Behavioral Pattern Analysis: MEDIUM Risk (Score: 45)
   • Transaction during high-risk hours (2 AM - 5 AM)
   • Transaction during unusual hours (late night/early morning)
   • First transaction for this customer (no historical pattern)

RECOMMENDED ACTIONS:
• BLOCK transaction immediately
• Contact customer to verify transaction legitimacy
• Flag account for enhanced monitoring
• Consider temporary account restrictions
• Document all findings for compliance review
```

### Short Summary:
```
Transaction REJECT - Payment originated from high-risk country (+4 more concerns)
```

---

## Key Features of ExplainabilityService

### 1. **Decision Header**
- Clear emoji-based visual indicator (🚫 REJECT, ⚠️ REVIEW, ✅ APPROVE)
- Transaction ID for reference
- Risk score out of 100
- Confidence percentage

### 2. **Risk Summary**
- Top 5 primary concerns in plain language
- Bullet-point format for easy scanning
- Prioritized by severity

### 3. **Detailed Analysis**
- Individual agent breakdowns
- Risk level classification (CRITICAL, HIGH, MEDIUM, LOW, MINIMAL)
- Specific findings from each agent
- Human-readable translations of technical reasons

### 4. **Recommended Actions**
- Decision-specific action items
- Clear next steps for fraud investigators
- Compliance and documentation guidance

### 5. **Short Summary**
- One-line summary for dashboards
- Includes top concern + count of additional issues
- Perfect for alerts and notifications

---

## Translation Examples

### Technical → Human-Readable

| Technical Reason | Human-Readable Translation |
|-----------------|---------------------------|
| "High-value transaction: 150000" | "Amount exceeds normal spending pattern" |
| "Transaction from high-risk country: RUSSIA" | "Payment originated from high-risk country" |
| "Suspicious device pattern detected: EMULATOR" | "Suspicious device characteristics detected (emulator, VPN, or rooted device)" |
| "New/unknown device for customer: UNKNOWN_DEVICE" | "Transaction from previously unknown device" |
| "High-risk merchant category: CRYPTO" | "Transaction with high-risk merchant category (crypto, gambling, forex)" |
| "High velocity: 6 transactions in 10 minutes" | "Unusually high transaction frequency detected" |
| "Potential structuring: amount just below reporting threshold" | "Potential structuring detected (amount just below reporting threshold)" |
| "Transaction during unusual hours: 3:00" | "Transaction during high-risk hours (2 AM - 5 AM)" |
| "First transaction for customer" | "First transaction for this customer (no historical pattern)" |

---

## Integration Points

### 1. **Fraud Orchestrator**
```java
// Automatically generates explanations after fraud analysis
FraudDecision decision = fraudOrchestratorService.analyzeTransaction(event);
// decision.getHumanReadableExplanation() - Full explanation
// decision.getShortSummary() - Dashboard summary
```

### 2. **REST API Response**
```json
{
  "txnId": "TXN1001",
  "finalScore": 85.50,
  "decision": "REJECT",
  "shortSummary": "Transaction REJECT - Amount exceeds normal spending pattern (+4 more concerns)",
  "humanReadableExplanation": "🚫 TRANSACTION REJECT\n..."
}
```

### 3. **Fraud Investigation Dashboard**
- Display full explanation in case details
- Show short summary in transaction list
- Use for email/SMS alerts to customers
- Include in compliance reports

---

## Benefits

1. **For Fraud Investigators:**
   - Clear, actionable insights
   - No technical jargon
   - Prioritized concerns
   - Specific next steps

2. **For Customers:**
   - Understandable explanations for blocked transactions
   - Transparency in fraud detection
   - Clear path to resolution

3. **For Compliance:**
   - Documented decision rationale
   - Audit trail of fraud analysis
   - Regulatory reporting support

4. **For Operations:**
   - Faster case resolution
   - Reduced false positives
   - Improved customer satisfaction
   - Better fraud detection accuracy

---

*Generated by ExplainabilityService - Fraud Investigation Platform v1.0.0*