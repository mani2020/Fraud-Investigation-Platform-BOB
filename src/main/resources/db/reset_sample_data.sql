-- ============================================================================
-- Database Reset Script for Sample Data
-- ============================================================================
-- Purpose: Delete all sample data to allow DataInitializer to repopulate
-- Usage: Run this script in PostgreSQL, then restart Spring Boot application
-- ============================================================================

-- Delete in correct order due to foreign key constraints
-- Note: fraud_alerts has FK to transactions with ON DELETE CASCADE,
-- so deleting transactions will automatically delete related fraud_alerts

DELETE FROM fraud_audit_logs;
DELETE FROM fraud_alerts;      -- Has FK to transactions
DELETE FROM transactions;
DELETE FROM trusted_devices;
DELETE FROM customer_profiles;

-- Verify deletion
SELECT 'fraud_audit_logs' as table_name, COUNT(*) as remaining_records FROM fraud_audit_logs
UNION ALL
SELECT 'fraud_alerts', COUNT(*) FROM fraud_alerts
UNION ALL
SELECT 'transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'trusted_devices', COUNT(*) FROM trusted_devices
UNION ALL
SELECT 'customer_profiles', COUNT(*) FROM customer_profiles;

-- ============================================================================
-- After running this script:
-- 1. Restart Spring Boot application
-- 2. DataInitializer will detect empty database (count = 0)
-- 3. 20 new sample transactions will be created via Kafka fraud detection flow
-- ============================================================================

-- Made with Bob