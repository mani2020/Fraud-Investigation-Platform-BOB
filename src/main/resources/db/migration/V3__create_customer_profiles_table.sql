-- Create customer_profiles table for storing customer behavior patterns
CREATE TABLE customer_profiles (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    country_code VARCHAR(3),
    registration_date DATE,
    kyc_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    risk_level VARCHAR(20) DEFAULT 'LOW', -- LOW, MEDIUM, HIGH, CRITICAL
    
    -- Transaction behavior patterns
    avg_transaction_amount DECIMAL(15,2) DEFAULT 0,
    max_transaction_amount DECIMAL(15,2) DEFAULT 0,
    total_transactions INTEGER DEFAULT 0,
    total_transaction_volume DECIMAL(15,2) DEFAULT 0,
    avg_daily_transactions INTEGER DEFAULT 0,
    
    -- Fraud history
    fraud_alerts_count INTEGER DEFAULT 0,
    false_positive_count INTEGER DEFAULT 0,
    confirmed_fraud_count INTEGER DEFAULT 0,
    last_fraud_alert_date TIMESTAMP,
    
    -- Trusted patterns
    trusted_countries TEXT[], -- Array of country codes
    trusted_merchants TEXT[], -- Array of merchant IDs
    usual_transaction_hours INTEGER[], -- Array of hours (0-23)
    
    -- Account status
    account_status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, BLOCKED, CLOSED
    suspension_reason TEXT,
    blocked_at TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_transaction_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_risk_level CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_kyc_status CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    CONSTRAINT chk_account_status CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'BLOCKED', 'CLOSED'))
);

-- Indexes for customer_profiles
CREATE INDEX idx_customer_profiles_customer_id ON customer_profiles(customer_id);
CREATE INDEX idx_customer_profiles_email ON customer_profiles(email);
CREATE INDEX idx_customer_profiles_risk_level ON customer_profiles(risk_level);
CREATE INDEX idx_customer_profiles_account_status ON customer_profiles(account_status);
CREATE INDEX idx_customer_profiles_kyc_status ON customer_profiles(kyc_status);
CREATE INDEX idx_customer_profiles_country_code ON customer_profiles(country_code);
CREATE INDEX idx_customer_profiles_last_transaction ON customer_profiles(last_transaction_at DESC);

-- Composite index for fraud monitoring
CREATE INDEX idx_customer_profiles_risk_fraud ON customer_profiles(risk_level, fraud_alerts_count DESC);

-- Comments
COMMENT ON TABLE customer_profiles IS 'Stores customer profiles with behavior patterns and fraud history';
COMMENT ON COLUMN customer_profiles.risk_level IS 'Customer risk level: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN customer_profiles.kyc_status IS 'KYC verification status: PENDING, VERIFIED, REJECTED';
COMMENT ON COLUMN customer_profiles.trusted_countries IS 'Array of country codes where customer frequently transacts';
COMMENT ON COLUMN customer_profiles.trusted_merchants IS 'Array of merchant IDs where customer frequently shops';
COMMENT ON COLUMN customer_profiles.usual_transaction_hours IS 'Array of hours (0-23) when customer typically transacts';

-- Made with Bob