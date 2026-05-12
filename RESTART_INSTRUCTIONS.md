# Restart Instructions - Apply Fraud Alerts Fix

## Problem
- Backend code has been updated to fix fraud alerts
- Changes won't take effect until Spring Boot is restarted
- Currently: 20 transactions exist but 0 alerts showing

## Solution: Restart Spring Boot Application

### Option 1: Complete Reset (Recommended)
This will reset the database and regenerate all sample data with proper alerts.

#### Step 1: Stop Spring Boot
If Spring Boot is running, stop it:
- Press `Ctrl+C` in the terminal where it's running
- Or close the terminal

#### Step 2: Reset Database
```powershell
.\scripts\reset-database.ps1
```
This will:
- Delete all existing transactions
- Clear all fraud alerts from memory
- Prepare for fresh data

#### Step 3: Restart Spring Boot
```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=default" "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
```

#### Step 4: Wait for Initialization
Watch the logs for:
```
Successfully submitted 20 sample transactions to Kafka for fraud detection
Fraud alert created: alertId=ALERT-..., severity=..., decision=...
```

You should see multiple "Fraud alert created" messages (5-10 alerts expected).

#### Step 5: Refresh Frontend
- Refresh your browser (F5)
- Navigate to "Active Fraud Alerts" tab
- You should now see alerts with different severity levels

---

### Option 2: Simple Restart (Keep Existing Data)
This keeps existing transactions but won't create alerts for them (alerts only created for NEW transactions).

#### Step 1: Stop Spring Boot
- Press `Ctrl+C` in the terminal
- Or close the terminal

#### Step 2: Restart Spring Boot
```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=default" "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
```

#### Step 3: Create New Transaction
To see alerts, you need to create a NEW transaction:

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "txnId": "TXN-TEST-001",
    "customerId": "CUST-00999",
    "amount": 9500.00,
    "merchant": "TestMerchant",
    "country": "Nigeria",
    "deviceId": "DEV-999",
    "paymentType": "CREDIT_CARD"
  }'
```

This high-risk transaction should trigger a BLOCK/HOLD decision and create an alert.

---

## Expected Results After Restart

### With Option 1 (Complete Reset):
- **Transactions**: 20 new transactions
- **Active Fraud Alerts**: 5-10 alerts
- **Alert Severities**: Mix of CRITICAL, HIGH, MEDIUM
- **Alert Decisions**: BLOCK, HOLD, OTP

### With Option 2 (Simple Restart):
- **Transactions**: Existing 20 transactions (no alerts for these)
- **Active Fraud Alerts**: 0 (until new transactions created)
- **New Transactions**: Will generate alerts properly

---

## Verification Steps

1. **Check Spring Boot Logs**
   Look for these log messages:
   ```
   Fraud alert created: alertId=ALERT-..., severity=CRITICAL, decision=BLOCK, txnId=...
   Fraud alert created: alertId=ALERT-..., severity=HIGH, decision=HOLD, txnId=...
   ```

2. **Check API Endpoint**
   ```bash
   curl http://localhost:8080/api/fraud-alerts
   ```
   Should return JSON array with alerts.

3. **Check Frontend**
   - Navigate to "Active Fraud Alerts" tab
   - Should see alerts listed with:
     - Alert ID
     - Severity badge (CRITICAL/HIGH/MEDIUM)
     - Transaction ID
     - Fraud Score
     - Timestamp

---

## Troubleshooting

### Still No Alerts After Restart?

1. **Check if Kafka is running**:
   ```bash
   docker ps
   ```
   Should show Kafka and Zookeeper containers running.

2. **Check Spring Boot logs for errors**:
   Look for any exceptions related to:
   - Kafka connection
   - Alert creation
   - Decision service

3. **Verify decision thresholds**:
   Check `application.yml` for:
   ```yaml
   fraud:
     decision:
       approve-threshold: 30
       otp-threshold: 50
       hold-threshold: 70
       block-threshold: 85
   ```

4. **Check transaction scores**:
   Most transactions might have low scores (< 50), so they won't trigger alerts.
   Use the curl command above to create a high-risk transaction.

---

## Summary of Changes Made

### Backend Changes:
1. **FraudOrchestratorService.java** - Alert creation now triggers on BLOCK, HOLD, and high-score OTP
2. **FraudNotificationService.java** - Severity mapping updated to match actual decision types

### Frontend Changes:
1. **Investigation.jsx** - Fixed percentage calculations (removed `* 100` multiplications)
2. **TransactionTable.jsx** - Fixed fraud score display
3. **TransactionMonitor.jsx** - Fixed score thresholds
4. **Dashboard.jsx** - Fixed average score calculation
5. **FraudAlerts.jsx** - Fixed API endpoint and score display
6. **Analytics.jsx** - Fixed status filtering and score calculations

---

*Made with Bob*