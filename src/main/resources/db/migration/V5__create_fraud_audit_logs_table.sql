-- Create fraud_audit_logs table for comprehensive audit trail
CREATE TABLE fraud_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    log_id VARCHAR(50) UNIQUE NOT NULL,
    
    -- Event identification
    event_type VARCHAR(50) NOT NULL, -- TRANSACTION_CREATED, FRAUD_DETECTED, ALERT_CREATED, DECISION_MADE, etc.
    event_category VARCHAR(30) NOT NULL, -- TRANSACTION, FRAUD_DETECTION, INVESTIGATION, SYSTEM
    severity VARCHAR(20) NOT NULL, -- INFO, WARNING, ERROR, CRITICAL
    
    -- Related entities
    txn_id VARCHAR(50),
    customer_id VARCHAR(50),
    alert_id VARCHAR(50),
    device_id VARCHAR(100),
    
    -- Agent information
    agent_name VARCHAR(50), -- Which fraud agent triggered this log
    agent_score INTEGER, -- Score from the agent (0-100)
    
    -- Event details
    event_description TEXT NOT NULL,
    event_data JSONB, -- Flexible JSON storage for event-specific data
    
    -- Decision information
    decision_type VARCHAR(20), -- APPROVE, OTP, HOLD, BLOCK
    decision_reason TEXT,
    previous_decision VARCHAR(20), -- For tracking decision changes
    
    -- User/System information
    performed_by VARCHAR(100), -- User ID or 'SYSTEM' for automated actions
    performed_by_role VARCHAR(50), -- SYSTEM, FRAUD_ANALYST, ADMIN, CUSTOMER
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    -- Investigation tracking
    investigation_id VARCHAR(50), -- Link to investigation case
    investigation_action VARCHAR(50), -- CASE_OPENED, CASE_ASSIGNED, CASE_RESOLVED, etc.
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_event_category CHECK (event_category IN ('TRANSACTION', 'FRAUD_DETECTION', 'INVESTIGATION', 'SYSTEM', 'CONFIGURATION')),
    CONSTRAINT chk_severity CHECK (severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')),
    CONSTRAINT chk_decision_type CHECK (decision_type IS NULL OR decision_type IN ('APPROVE', 'OTP', 'HOLD', 'BLOCK'))
);

-- Indexes for fraud_audit_logs
CREATE INDEX idx_fraud_audit_logs_txn_id ON fraud_audit_logs(txn_id);
CREATE INDEX idx_fraud_audit_logs_customer_id ON fraud_audit_logs(customer_id);
CREATE INDEX idx_fraud_audit_logs_alert_id ON fraud_audit_logs(alert_id);
CREATE INDEX idx_fraud_audit_logs_device_id ON fraud_audit_logs(device_id);
CREATE INDEX idx_fraud_audit_logs_event_type ON fraud_audit_logs(event_type);
CREATE INDEX idx_fraud_audit_logs_event_category ON fraud_audit_logs(event_category);
CREATE INDEX idx_fraud_audit_logs_severity ON fraud_audit_logs(severity);
CREATE INDEX idx_fraud_audit_logs_created_at ON fraud_audit_logs(created_at DESC);
CREATE INDEX idx_fraud_audit_logs_event_timestamp ON fraud_audit_logs(event_timestamp DESC);
CREATE INDEX idx_fraud_audit_logs_performed_by ON fraud_audit_logs(performed_by);
CREATE INDEX idx_fraud_audit_logs_investigation_id ON fraud_audit_logs(investigation_id) WHERE investigation_id IS NOT NULL;

-- Composite indexes for common queries
CREATE INDEX idx_fraud_audit_logs_customer_event ON fraud_audit_logs(customer_id, event_timestamp DESC);
CREATE INDEX idx_fraud_audit_logs_txn_event ON fraud_audit_logs(txn_id, event_timestamp DESC);
CREATE INDEX idx_fraud_audit_logs_category_severity ON fraud_audit_logs(event_category, severity, created_at DESC);

-- GIN index for JSONB queries
CREATE INDEX idx_fraud_audit_logs_event_data ON fraud_audit_logs USING GIN(event_data);

-- Partial indexes for critical events
CREATE INDEX idx_fraud_audit_logs_critical ON fraud_audit_logs(created_at DESC) 
    WHERE severity = 'CRITICAL';
CREATE INDEX idx_fraud_audit_logs_fraud_events ON fraud_audit_logs(created_at DESC) 
    WHERE event_category = 'FRAUD_DETECTION';

-- Comments
COMMENT ON TABLE fraud_audit_logs IS 'Comprehensive audit trail for all fraud detection and investigation activities';
COMMENT ON COLUMN fraud_audit_logs.event_type IS 'Specific event type (e.g., TRANSACTION_CREATED, FRAUD_DETECTED, ALERT_CREATED)';
COMMENT ON COLUMN fraud_audit_logs.event_category IS 'High-level category: TRANSACTION, FRAUD_DETECTION, INVESTIGATION, SYSTEM';
COMMENT ON COLUMN fraud_audit_logs.event_data IS 'Flexible JSONB field for storing event-specific data';
COMMENT ON COLUMN fraud_audit_logs.performed_by IS 'User ID or SYSTEM for automated actions';
COMMENT ON COLUMN fraud_audit_logs.investigation_id IS 'Links audit log to investigation case';

-- Made with Bob