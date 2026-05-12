# Fraud Alerts Fix - Decision Type Mismatch

## Problem
- **Notification showing 20 data**: All 20 transactions were being processed
- **Active Fraud Alerts showing 0 data**: No alerts were being created

## Root Cause
Critical mismatch between decision types:

### What Was Expected
The orchestrator was checking for:
- `"REVIEW"` decisions
- `"REJECT"` decisions

### What Was Actually Generated
The `DecisionService` returns:
- `"APPROVE"` (score < 30)
- `"OTP"` (score 30-84)
- `"HOLD"` (score 70-84)
- `"BLOCK"` (score ≥ 85)

**Result**: The condition `if ("REVIEW".equals(...) || "REJECT".equals(...))` NEVER matched, so NO alerts were ever created!

## Solution

### 1. Fixed Alert Creation Logic
**File**: `src/main/java/com/fraud/platform/orchestrator/FraudOrchestratorService.java`

**Before**:
```java
if ("REVIEW".equals(decision.getDecision()) || "REJECT".equals(decision.getDecision())) {
    // Create alert
}
```

**After**:
```java
String decisionType = decision.getDecision();
BigDecimal score = decision.getFinalScore();

if ("BLOCK".equals(decisionType) || "HOLD".equals(decisionType) || 
    ("OTP".equals(decisionType) && score.compareTo(BigDecimal.valueOf(50)) >= 0)) {
    // Create alert
}
```

### 2. Fixed Severity Mapping
**File**: `src/main/java/com/fraud/platform/service/FraudNotificationService.java`

**Updated severity determination**:
- `BLOCK` or score ≥ 85 → **CRITICAL**
- `HOLD` or score ≥ 70 → **HIGH**
- `OTP` or score ≥ 50 → **MEDIUM**
- Otherwise → **LOW**

**Updated alert messages**:
- `BLOCK` → "Transaction blocked - Fraud detected (Score: X%)"
- `HOLD` → "Transaction on hold - High risk (Score: X%)"
- `OTP` → "OTP required - Suspicious activity (Score: X%)"

### 3. Fixed Alert Type Detection
Changed from checking `"REJECT"` to checking `"BLOCK"` for fraud detection alerts.

## Expected Behavior After Fix

### Alert Creation Triggers
Alerts will now be created for:
1. **All BLOCK decisions** (score ≥ 85) → CRITICAL alerts
2. **All HOLD decisions** (score 70-84) → HIGH alerts
3. **High-score OTP decisions** (score 50-69) → MEDIUM alerts

### Sample Data Impact
With 20 random transactions:
- Approximately 2-4 transactions should trigger BLOCK/HOLD decisions
- Approximately 3-6 transactions should trigger high-score OTP decisions
- **Total expected alerts**: 5-10 alerts (instead of 0)

## Testing Steps

1. **Reset database** (to clear old transactions):
   ```powershell
   .\scripts\reset-database.ps1
   ```

2. **Restart Spring Boot**:
   ```bash
   mvn spring-boot:run
   ```

3. **Verify alerts are created**:
   - Check logs for "Fraud alert created" messages
   - Navigate to Active Fraud Alerts tab
   - Should see multiple alerts with different severity levels

4. **Test API endpoint**:
   ```bash
   curl http://localhost:8080/api/fraud-alerts
   ```

## Related Files Modified
- `src/main/java/com/fraud/platform/orchestrator/FraudOrchestratorService.java`
- `src/main/java/com/fraud/platform/service/FraudNotificationService.java`

## Decision Type Reference

| Score Range | Decision | Alert Created | Severity |
|-------------|----------|---------------|----------|
| 0-29        | APPROVE  | No            | -        |
| 30-49       | OTP      | No            | -        |
| 50-69       | OTP      | Yes           | MEDIUM   |
| 70-84       | HOLD     | Yes           | HIGH     |
| 85-100      | BLOCK    | Yes           | CRITICAL |

---
*Made with Bob*