-- Create trusted_devices table for device fingerprinting and trust management
CREATE TABLE trusted_devices (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(100) UNIQUE NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    device_fingerprint VARCHAR(255) NOT NULL,
    
    -- Device information
    device_type VARCHAR(50), -- MOBILE, TABLET, DESKTOP, UNKNOWN
    os_name VARCHAR(50),
    os_version VARCHAR(50),
    browser_name VARCHAR(50),
    browser_version VARCHAR(50),
    user_agent TEXT,
    
    -- Device trust status
    trust_status VARCHAR(20) DEFAULT 'NEW', -- NEW, TRUSTED, SUSPICIOUS, BLOCKED
    trust_score INTEGER DEFAULT 0, -- 0-100
    
    -- Usage statistics
    first_seen_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_count INTEGER DEFAULT 0,
    successful_transactions INTEGER DEFAULT 0,
    failed_transactions INTEGER DEFAULT 0,
    fraud_attempts INTEGER DEFAULT 0,
    
    -- Location tracking
    first_seen_country VARCHAR(3),
    first_seen_ip VARCHAR(45),
    last_seen_country VARCHAR(3),
    last_seen_ip VARCHAR(45),
    countries_used TEXT[], -- Array of country codes
    
    -- Security flags
    is_rooted_jailbroken BOOLEAN DEFAULT FALSE,
    is_emulator BOOLEAN DEFAULT FALSE,
    is_vpn_proxy BOOLEAN DEFAULT FALSE,
    has_suspicious_apps BOOLEAN DEFAULT FALSE,
    
    -- Device sharing detection
    shared_with_customers TEXT[], -- Array of customer IDs using same device
    is_shared_device BOOLEAN DEFAULT FALSE,
    
    -- Status management
    blocked_at TIMESTAMP,
    blocked_reason TEXT,
    unblocked_at TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_trust_status CHECK (trust_status IN ('NEW', 'TRUSTED', 'SUSPICIOUS', 'BLOCKED')),
    CONSTRAINT chk_device_type CHECK (device_type IN ('MOBILE', 'TABLET', 'DESKTOP', 'UNKNOWN')),
    CONSTRAINT chk_trust_score CHECK (trust_score >= 0 AND trust_score <= 100)
);

-- Indexes for trusted_devices
CREATE INDEX idx_trusted_devices_device_id ON trusted_devices(device_id);
CREATE INDEX idx_trusted_devices_customer_id ON trusted_devices(customer_id);
CREATE INDEX idx_trusted_devices_fingerprint ON trusted_devices(device_fingerprint);
CREATE INDEX idx_trusted_devices_trust_status ON trusted_devices(trust_status);
CREATE INDEX idx_trusted_devices_last_seen ON trusted_devices(last_seen_at DESC);
CREATE INDEX idx_trusted_devices_is_shared ON trusted_devices(is_shared_device) WHERE is_shared_device = TRUE;

-- Composite indexes for fraud detection queries
CREATE INDEX idx_trusted_devices_customer_trust ON trusted_devices(customer_id, trust_status);
CREATE INDEX idx_trusted_devices_suspicious ON trusted_devices(trust_status, fraud_attempts DESC) 
    WHERE trust_status IN ('SUSPICIOUS', 'BLOCKED');

-- GIN index for array searches
CREATE INDEX idx_trusted_devices_countries ON trusted_devices USING GIN(countries_used);
CREATE INDEX idx_trusted_devices_shared_customers ON trusted_devices USING GIN(shared_with_customers);

-- Comments
COMMENT ON TABLE trusted_devices IS 'Stores device fingerprints and trust information for fraud detection';
COMMENT ON COLUMN trusted_devices.device_fingerprint IS 'Unique device fingerprint hash';
COMMENT ON COLUMN trusted_devices.trust_status IS 'Device trust level: NEW, TRUSTED, SUSPICIOUS, BLOCKED';
COMMENT ON COLUMN trusted_devices.trust_score IS 'Device trust score from 0 (untrusted) to 100 (fully trusted)';
COMMENT ON COLUMN trusted_devices.is_shared_device IS 'Flag indicating if device is used by multiple customers';
COMMENT ON COLUMN trusted_devices.shared_with_customers IS 'Array of customer IDs that have used this device';

-- Made with Bob