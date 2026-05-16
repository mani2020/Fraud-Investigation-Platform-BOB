-- V7: Add JSONB columns for nested fraud event data
-- This migration adds support for storing rich nested data structures from CanonicalFraudEvent

-- Add JSONB columns for nested fraud event data
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS customer_data JSONB;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS merchant_data JSONB;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS device_data JSONB;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS location_data JSONB;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS behavior_metrics JSONB;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS fraud_signals JSONB;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS metadata JSONB;

-- Add GIN indexes for JSONB columns for better query performance
-- GIN indexes are optimal for JSONB data and support containment queries
CREATE INDEX IF NOT EXISTS idx_transactions_customer_data ON transactions USING GIN (customer_data);
CREATE INDEX IF NOT EXISTS idx_transactions_merchant_data ON transactions USING GIN (merchant_data);
CREATE INDEX IF NOT EXISTS idx_transactions_device_data ON transactions USING GIN (device_data);
CREATE INDEX IF NOT EXISTS idx_transactions_fraud_signals ON transactions USING GIN (fraud_signals);

-- Add comments for documentation
COMMENT ON COLUMN transactions.customer_data IS 'Customer information in JSONB format (CustomerInfo)';
COMMENT ON COLUMN transactions.merchant_data IS 'Merchant information in JSONB format (MerchantInfo)';
COMMENT ON COLUMN transactions.device_data IS 'Device information in JSONB format (DeviceInfo)';
COMMENT ON COLUMN transactions.location_data IS 'Location information in JSONB format (LocationInfo)';
COMMENT ON COLUMN transactions.behavior_metrics IS 'Behavior metrics in JSONB format (BehaviorMetrics)';
COMMENT ON COLUMN transactions.fraud_signals IS 'Fraud signals in JSONB format (FraudSignals)';
COMMENT ON COLUMN transactions.metadata IS 'Additional metadata in JSONB format (MetadataInfo)';

-- Made with Bob
