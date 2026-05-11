# Database Schema Documentation

## Overview
This document describes the PostgreSQL database schema for the Fraud Investigation Platform. The schema is designed to support real-time fraud detection, investigation workflows, and comprehensive audit trails.

## Schema Migrations

All schema changes are managed through Flyway migrations located in `src/main/resources/db/migration/`:

- **V1__create_transactions_table.sql** - Core transaction data
- **V2__create_fraud_alerts_table.sql** - Fraud detection alerts
- **V3__create_customer_profiles_table.sql** - Customer behavior profiles
- **V4__create_trusted_devices_table.sql** - Device fingerprinting and trust
- **V5__create_fraud_audit_logs_table.sql** - Comprehensive audit trail

## Tables

### 1. transactions
**Purpose**: Stores all payment transactions for fraud analysis

**Key Fields**:
- `txn_id` - Unique transaction identifier
- `customer_id` - Customer identifier
- `amount` - Transaction amount
- `currency` - Currency code (USD, EUR, etc.)
- `merchant_id`, `merchant_name`, `merchant_category` - Merchant details
- `payment_method` - CARD, UPI, WALLET, BANK_TRANSFER
- `device_id`, `device_fingerprint` - Device identification
- `ip_address`, `country_code` - Location data
- `fraud_score` - Calculated fraud risk score (0-100)
- `fraud_decision` - APPROVE, OTP, HOLD, BLOCK
- `status` - PENDING, APPROVED, DECLINED, UNDER_REVIEW

**Indexes**:
- Primary key on `id`
- Unique index on `txn_id`
- Indexes on `customer_id`, `merchant_id`, `device_id`, `status`, `fraud_decision`
- Composite indexes for fraud monitoring queries

### 2. fraud_alerts
**Purpose**: Stores fraud detection alerts for investigation

**Key Fields**:
- `alert_id` - Unique alert identifier
- `txn_id` - Related transaction
- `customer_id` - Customer identifier
- `alert_type` - BLOCK, HOLD, OTP, REVIEW
- `severity` - CRITICAL, HIGH, MEDIUM, LOW
- `fraud_score` - Aggregated fraud score
- `triggered_agents` - Array of agent names that triggered alert
- `alert_reasons` - Detailed reasons (JSON/text)
- `status` - OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE
- `assigned_to` - Fraud analyst assigned
- `resolution_notes` - Investigation outcome

**Indexes**:
- Primary key on `id`
- Unique index on `alert_id`
- Foreign key to `transactions(txn_id)`
- Indexes on `customer_id`, `status`, `severity`, `assigned_to`
- Composite index for analyst dashboard queries

### 3. customer_profiles
**Purpose**: Stores customer behavior patterns and fraud history

**Key Fields**:
- `customer_id` - Unique customer identifier
- `kyc_status` - PENDING, VERIFIED, REJECTED
- `risk_level` - LOW, MEDIUM, HIGH, CRITICAL
- `avg_transaction_amount`, `max_transaction_amount` - Spending patterns
- `total_transactions`, `total_transaction_volume` - Transaction history
- `fraud_alerts_count`, `confirmed_fraud_count` - Fraud history
- `trusted_countries`, `trusted_merchants` - Trusted patterns (arrays)
- `usual_transaction_hours` - Array of typical transaction hours (0-23)
- `account_status` - ACTIVE, SUSPENDED, BLOCKED, CLOSED

**Indexes**:
- Primary key on `id`
- Unique index on `customer_id`
- Indexes on `email`, `risk_level`, `account_status`, `kyc_status`
- Composite index for fraud monitoring

### 4. trusted_devices
**Purpose**: Device fingerprinting and trust management

**Key Fields**:
- `device_id` - Unique device identifier
- `customer_id` - Customer identifier
- `device_fingerprint` - Unique device fingerprint hash
- `device_type` - MOBILE, TABLET, DESKTOP, UNKNOWN
- `os_name`, `os_version`, `browser_name`, `browser_version` - Device info
- `trust_status` - NEW, TRUSTED, SUSPICIOUS, BLOCKED
- `trust_score` - Device trust score (0-100)
- `transaction_count`, `successful_transactions`, `fraud_attempts` - Usage stats
- `is_rooted_jailbroken`, `is_emulator`, `is_vpn_proxy` - Security flags
- `is_shared_device`, `shared_with_customers` - Device sharing detection

**Indexes**:
- Primary key on `id`
- Unique index on `device_id`
- Indexes on `customer_id`, `device_fingerprint`, `trust_status`
- GIN indexes on array fields (`countries_used`, `shared_with_customers`)
- Composite indexes for fraud detection queries

### 5. fraud_audit_logs
**Purpose**: Comprehensive audit trail for all fraud-related activities

**Key Fields**:
- `log_id` - Unique log identifier
- `event_type` - Specific event (TRANSACTION_CREATED, FRAUD_DETECTED, etc.)
- `event_category` - TRANSACTION, FRAUD_DETECTION, INVESTIGATION, SYSTEM
- `severity` - INFO, WARNING, ERROR, CRITICAL
- `txn_id`, `customer_id`, `alert_id`, `device_id` - Related entities
- `agent_name`, `agent_score` - Fraud agent information
- `event_description`, `event_data` - Event details (JSONB)
- `decision_type`, `decision_reason` - Decision information
- `performed_by`, `performed_by_role` - User/system information
- `investigation_id`, `investigation_action` - Investigation tracking

**Indexes**:
- Primary key on `id`
- Unique index on `log_id`
- Indexes on all entity IDs, event types, severity, timestamps
- GIN index on `event_data` JSONB field
- Partial indexes for critical events and fraud events

## Relationships

```
transactions (1) ←→ (N) fraud_alerts
    ↓
    └─→ customer_profiles (via customer_id)
    └─→ trusted_devices (via device_id)
    └─→ fraud_audit_logs (via txn_id)

customer_profiles (1) ←→ (N) trusted_devices
    ↓
    └─→ fraud_alerts (via customer_id)
    └─→ fraud_audit_logs (via customer_id)

fraud_alerts (1) ←→ (N) fraud_audit_logs
```

## Sample Data for Testing

### High-Risk Transaction (Should BLOCK)
```sql
INSERT INTO transactions (txn_id, customer_id, amount, currency, merchant_id, merchant_name, merchant_category, payment_method, device_id, device_fingerprint, ip_address, country_code, status)
VALUES ('TXN-HIGH-RISK-001', 'CUST-001', 150000.00, 'USD', 'MERCH-CRYPTO-001', 'CryptoExchange', 'CRYPTOCURRENCY', 'CARD', 'DEV-NEW-001', 'fp-suspicious-001', '185.220.101.50', 'RU', 'PENDING');
```

### Medium-Risk Transaction (Should OTP)
```sql
INSERT INTO transactions (txn_id, customer_id, amount, currency, merchant_id, merchant_name, merchant_category, payment_method, device_id, device_fingerprint, ip_address, country_code, status)
VALUES ('TXN-MEDIUM-RISK-001', 'CUST-002', 75000.00, 'USD', 'MERCH-RETAIL-001', 'OnlineRetailer', 'RETAIL', 'CARD', 'DEV-NEW-002', 'fp-new-device-001', '203.0.113.45', 'IN', 'PENDING');
```

### Low-Risk Transaction (Should APPROVE)
```sql
INSERT INTO transactions (txn_id, customer_id, amount, currency, merchant_id, merchant_name, merchant_category, payment_method, device_id, device_fingerprint, ip_address, country_code, status)
VALUES ('TXN-LOW-RISK-001', 'CUST-003', 5000.00, 'USD', 'MERCH-GROCERY-001', 'LocalGrocery', 'GROCERY', 'UPI', 'DEV-TRUSTED-001', 'fp-trusted-001', '203.0.113.100', 'US', 'PENDING');
```

### Sample Customer Profile
```sql
INSERT INTO customer_profiles (customer_id, first_name, last_name, email, phone, country_code, registration_date, kyc_status, risk_level, avg_transaction_amount, max_transaction_amount, total_transactions, trusted_countries, account_status)
VALUES ('CUST-001', 'John', 'Doe', 'john.doe@example.com', '+1234567890', 'US', '2023-01-15', 'VERIFIED', 'LOW', 5000.00, 25000.00, 150, ARRAY['US', 'CA', 'GB'], 'ACTIVE');
```

### Sample Trusted Device
```sql
INSERT INTO trusted_devices (device_id, customer_id, device_fingerprint, device_type, os_name, browser_name, trust_status, trust_score, transaction_count, successful_transactions, first_seen_country, last_seen_country)
VALUES ('DEV-TRUSTED-001', 'CUST-003', 'fp-trusted-001', 'MOBILE', 'iOS', 'Safari', 'TRUSTED', 95, 200, 198, 'US', 'US');
```

## Running Migrations

### Start Infrastructure
```bash
docker-compose -f docker-compose-dev.yml up -d
```

### Run Application (Flyway auto-migrates)
```bash
mvn spring-boot:run
```

### Manual Migration Check
```bash
mvn flyway:info
mvn flyway:migrate
```

## Database Queries for Testing

### Check All Tables
```sql
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;
```

### View Transaction with Fraud Score
```sql
SELECT txn_id, customer_id, amount, merchant_name, 
       fraud_score, fraud_decision, status 
FROM transactions 
ORDER BY created_at DESC 
LIMIT 10;
```

### View Open Fraud Alerts
```sql
SELECT a.alert_id, a.txn_id, a.customer_id, a.severity, 
       a.fraud_score, a.status, t.amount, t.merchant_name
FROM fraud_alerts a
JOIN transactions t ON a.txn_id = t.txn_id
WHERE a.status = 'OPEN'
ORDER BY a.created_at DESC;
```

### View Customer Risk Profile
```sql
SELECT customer_id, risk_level, fraud_alerts_count, 
       confirmed_fraud_count, total_transactions, 
       avg_transaction_amount, account_status
FROM customer_profiles
WHERE risk_level IN ('HIGH', 'CRITICAL')
ORDER BY fraud_alerts_count DESC;
```

### View Suspicious Devices
```sql
SELECT device_id, customer_id, trust_status, trust_score, 
       fraud_attempts, is_shared_device, transaction_count
FROM trusted_devices
WHERE trust_status IN ('SUSPICIOUS', 'BLOCKED')
   OR fraud_attempts > 0
ORDER BY fraud_attempts DESC;
```

### View Recent Audit Logs
```sql
SELECT log_id, event_type, event_category, severity, 
       txn_id, customer_id, agent_name, decision_type, 
       performed_by, created_at
FROM fraud_audit_logs
WHERE event_category = 'FRAUD_DETECTION'
ORDER BY created_at DESC
LIMIT 20;
```

## Performance Considerations

1. **Indexes**: All tables have appropriate indexes for common query patterns
2. **Partitioning**: Consider partitioning `fraud_audit_logs` by date for large volumes
3. **Archiving**: Implement archiving strategy for old transactions and audit logs
4. **JSONB**: Use JSONB for flexible event data with GIN indexes for fast queries
5. **Arrays**: PostgreSQL arrays for multi-valued fields (countries, agents, etc.)

## Security Considerations

1. **PII Protection**: Customer email, phone stored in `customer_profiles`
2. **Audit Trail**: All fraud decisions logged in `fraud_audit_logs`
3. **Data Retention**: Define retention policies for each table
4. **Access Control**: Implement row-level security for sensitive data
5. **Encryption**: Consider encryption at rest for sensitive fields

## Made with Bob