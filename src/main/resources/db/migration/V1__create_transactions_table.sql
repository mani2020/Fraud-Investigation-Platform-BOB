-- Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    txn_id VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    merchant VARCHAR(100) NOT NULL,
    country VARCHAR(50) NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    fraud_score DECIMAL(5,2),
    fraud_decision VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_transactions_customer_timestamp ON transactions(customer_id, timestamp);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_txn_id ON transactions(txn_id);
CREATE INDEX idx_transactions_fraud_decision ON transactions(fraud_decision);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp DESC);

-- Add comments
COMMENT ON TABLE transactions IS 'Payment transactions for fraud detection';
COMMENT ON COLUMN transactions.txn_id IS 'Unique transaction identifier';
COMMENT ON COLUMN transactions.status IS 'Transaction status: PENDING, PROCESSING, PROCESSED';
COMMENT ON COLUMN transactions.fraud_score IS 'Fraud score from 0-100';
COMMENT ON COLUMN transactions.fraud_decision IS 'Fraud decision: APPROVE, REVIEW, BLOCK';

-- Made with Bob
