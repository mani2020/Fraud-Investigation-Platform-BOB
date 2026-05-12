# ============================================================================
# Database Reset Script for Fraud Investigation Platform
# ============================================================================
# Purpose: Delete all sample data and allow DataInitializer to repopulate
# Usage: .\scripts\reset-database.ps1
# ============================================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fraud Investigation Platform" -ForegroundColor Cyan
Write-Host "Database Reset Script" -ForegroundColor Cyan
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
Write-Host "WARNING: This will delete ALL data from the following tables:" -ForegroundColor Yellow
Write-Host "  - fraud_audit_logs" -ForegroundColor Yellow
Write-Host "  - fraud_alerts" -ForegroundColor Yellow
Write-Host "  - transactions" -ForegroundColor Yellow
Write-Host "  - trusted_devices" -ForegroundColor Yellow
Write-Host "  - customer_profiles" -ForegroundColor Yellow
Write-Host ""
$confirmation = Read-Host "Are you sure you want to continue? (yes/no)"

if ($confirmation -ne "yes") {
    Write-Host "Operation cancelled." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Deleting data from database..." -ForegroundColor Yellow

# Execute DELETE commands
$deleteCommand = "DELETE FROM fraud_audit_logs; DELETE FROM fraud_alerts; DELETE FROM transactions; DELETE FROM trusted_devices; DELETE FROM customer_profiles;"

docker exec -it fraud-postgres psql -U fraud -d frauddb -c $deleteCommand

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Database reset completed successfully!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Restart Spring Boot application: mvn spring-boot:run" -ForegroundColor White
    Write-Host "2. DataInitializer will create 20 new sample transactions" -ForegroundColor White
    Write-Host "3. Transactions will be processed through Kafka fraud detection" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "ERROR: Failed to reset database!" -ForegroundColor Red
    Write-Host "Please check the error message above." -ForegroundColor Red
    exit 1
}

# Made with Bob