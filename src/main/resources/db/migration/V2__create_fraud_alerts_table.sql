-- Create fraud_alerts table for storing fraud detection alerts
CREATE TABLE fraud_alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_id VARCHAR(50) UNIQUE NOT NULL,
    txn_id VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    alert_type VARCHAR(20) NOT NULL, -- BLOCK, HOLD, OTP, REVIEW
    severity VARCHAR(20) NOT NULL, -- CRITICAL, HIGH, MEDIUM, LOW
    fraud_score DECIMAL(5,2) NOT NULL,
    triggered_agents TEXT[], -- Array of agent names that triggered alert
    alert_reasons TEXT NOT NULL, -- JSON or text with reasons
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE
    assigned_to VARCHAR(100), -- Fraud analyst assigned
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    
    -- Foreign key to transactions
    CONSTRAINT fk_fraud_alerts_txn FOREIGN KEY (txn_id) 
        REFERENCES transactions(txn_id) ON DELETE CASCADE
);

-- Indexes for fraud_alerts
CREATE INDEX idx_fraud_alerts_customer_id ON fraud_alerts(customer_id);
CREATE INDEX idx_fraud_alerts_txn_id ON fraud_alerts(txn_id);
CREATE INDEX idx_fraud_alerts_status ON fraud_alerts(status);
CREATE INDEX idx_fraud_alerts_severity ON fraud_alerts(severity);
CREATE INDEX idx_fraud_alerts_created_at ON fraud_alerts(created_at DESC);
CREATE INDEX idx_fraud_alerts_assigned_to ON fraud_alerts(assigned_to) WHERE assigned_to IS NOT NULL;

-- Composite index for analyst dashboard queries
CREATE INDEX idx_fraud_alerts_status_severity ON fraud_alerts(status, severity, created_at DESC);

-- Comments
COMMENT ON TABLE fraud_alerts IS 'Stores fraud detection alerts for investigation and tracking';
COMMENT ON COLUMN fraud_alerts.alert_type IS 'Type of alert: BLOCK, HOLD, OTP, REVIEW';
COMMENT ON COLUMN fraud_alerts.severity IS 'Alert severity: CRITICAL, HIGH, MEDIUM, LOW';
COMMENT ON COLUMN fraud_alerts.triggered_agents IS 'Array of fraud detection agents that triggered this alert';
COMMENT ON COLUMN fraud_alerts.status IS 'Alert status: OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE';

-- Made with Bob