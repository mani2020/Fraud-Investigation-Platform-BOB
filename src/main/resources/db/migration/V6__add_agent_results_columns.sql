-- Add columns to store agent results and risk factors as JSON
ALTER TABLE transactions ADD COLUMN agent_results TEXT;
ALTER TABLE transactions ADD COLUMN risk_factors TEXT;

-- Made with Bob
