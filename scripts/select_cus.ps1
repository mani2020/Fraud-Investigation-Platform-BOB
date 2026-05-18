# ============================================================================
# Database Reset Script for Fraud Investigation Platform
# ============================================================================
# Purpose: Delete all sample data and allow DataInitializer to repopulate
# Usage: .\scripts\reset-database.ps1
#
# Database Schema: Includes migrations V1-V7
#   V1: transactions table (base schema)
#   V2: fraud_alerts table
#   V3: customer_profiles table
#   V4: trusted_devices table
#   V5: fraud_audit_logs table
#   V6: (if exists)
#   V7: JSONB columns for nested fraud data (customer_data, merchant_data,
#       device_data, location_data, behavior_metrics, fraud_signals, metadata)
#
# Note: Flyway migrations run automatically on Spring Boot startup.
#       This script only deletes data, not schema.
# ============================================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fraud Investigation Platform" -ForegroundColor Cyan
Write-Host "Database Select Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker status..." -ForegroundColor Yellow
$dockerRunning = docker ps 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker is not running or not installed!" -ForegroundColor Red
    Write-Host "Please start Docker Desktop and try again." -ForegroundColor Red
    exit 1
}

# Check if fraud-postgres container is running
Write-Host "Checking PostgreSQL container..." -ForegroundColor Yellow
$postgresContainer = docker ps --filter "name=fraud-postgres" --format "{{.Names}}"
if ($postgresContainer -ne "fraud-postgres") {
    Write-Host "ERROR: PostgreSQL container 'fraud-postgres' is not running!" -ForegroundColor Red
    Write-Host "Start it with: docker-compose -f docker-compose-dev.yml up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "PostgreSQL container is running." -ForegroundColor Green
Write-Host ""

# Confirm deletion
Write-Host "SELECT txn_id, amount, timestamp FROM transactions WHERE customer_id = 'CUST-20001' ORDER BY timestamp;" -ForegroundColor Yellow

Write-Host ""


Write-Host ""
Write-Host "select data from database..." -ForegroundColor Yellow

# Execute DELETE commands
$selectCommand = "\COPY (SELECT * FROM transactions) TO STDOUT WITH CSV HEADER"

$outputFile = "transaction-results.csv"

docker exec fraud-postgres `
    psql -U fraud -d frauddb -c $selectCommand `
    > $outputFile

Write-Host ""
Write-Host "Results exported to: $outputFile" -ForegroundColor Green

# Made with Bob