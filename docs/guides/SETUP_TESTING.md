# Setup & Testing Guide - Fraud Investigation Platform

## 📋 Exact Setup Information

### System Requirements
- **Java:** 17 or higher
- **Maven:** 3.6+
- **Docker Desktop:** Required for full Kafka-based fraud detection
- **Operating System:** Windows 11, macOS, or Linux

### Project Structure
```
fraud-investigation-platform/
├── src/main/java/com/fraud/platform/    # Java source code
├── src/main/resources/                   # Configuration files
├── docker-compose-dev.yml                # Docker infrastructure
├── pom.xml                               # Maven dependencies
└── logs/                                 # Application logs (auto-created)
```

### Key Configuration Files
- **[`application.yml`](src/main/resources/application.yml)** - Main application configuration
- **[`application-h2.yml`](src/main/resources/application-h2.yml)** - H2 in-memory database profile
- **[`docker-compose-dev.yml`](docker-compose-dev.yml)** - PostgreSQL, Kafka, Zookeeper
- **[`logback-spring.xml`](src/main/resources/logback-spring.xml)** - Logging configuration

### Application Ports
- **8080** - Spring Boot application
- **5432** - PostgreSQL database
- **9092** - Kafka broker
- **2181** - Zookeeper

### Database Credentials

**PostgreSQL (Docker):**
- Host: `localhost:5432`
- Database: `frauddb`
- Username: `frauduser`
- Password: `fraudpass`

**H2 (In-Memory):**
- Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:frauddb`
- Username: `sa`
- Password: (leave blank)

### Kafka Topics
- **fraud-transactions** - Incoming transaction events
- **fraud-decisions** - Fraud detection results

---

## 🚀 Quick Start (2 Options)

### Option 1: Full Setup with Docker (Recommended)

**Step 1: Start Docker Infrastructure**
```bash
docker-compose -f docker-compose-dev.yml up -d
```

**Step 2: Verify Containers**
```bash
docker ps
```
Expected: 3 containers running (postgres, kafka, zookeeper)

**Step 3: Start Application**
```bash
mvn spring-boot:run
```

**Step 4: Wait for Startup**
Look for: `Started FraudPlatformApplication in X seconds`

**Application Ready:** `http://localhost:8080`

---

### Option 2: Quick Testing with H2 (No Docker)

**Step 1: Start with H2 Profile**
```powershell
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="h2"; mvn spring-boot:run
```

```bash
# Linux/Mac
SPRING_PROFILES_ACTIVE=h2 mvn spring-boot:run
```

**Step 2: Wait for Startup**
Look for: `Started FraudPlatformApplication in X seconds`

**Application Ready:** `http://localhost:8080`

**Note:** Kafka fraud detection will show connection errors (expected), but API will work.

---

## 🧪 Testing the Application

### Test 1: Simple Transaction (Verify API Works)

**PowerShell:**
```powershell
$body = @{
    txnId = "TXN-001"
    customerId = "CUST-001"
    amount = 5000.00
    merchant = "LocalGrocery"
    country = "US"
    deviceId = "DEV-001"
    paymentType = "UPI"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/transactions" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

**Bash/curl:**
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "txnId": "TXN-001",
    "customerId": "CUST-001",
    "amount": 5000.00,
    "merchant": "LocalGrocery",
    "country": "US",
    "deviceId": "DEV-001",
    "paymentType": "UPI"
  }'
```

**Expected Response:**
```json
{
  "txnId": "TXN-001",
  "status": "PENDING",
  "message": "Transaction created successfully"
}
```

---

### Test 2: High-Risk Transaction (With Docker/Kafka)

**PowerShell:**
```powershell
$body = @{
    txnId = "TXN-002"
    customerId = "CUST-002"
    amount = 150000.00
    merchant = "CryptoExchange"
    country = "RU"
    deviceId = "DEV-SUSPICIOUS-001"
    paymentType = "CARD"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/transactions" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

**Expected in Application Logs:**
```
=== INCOMING REQUEST ===
Request ID: 550e8400-e29b-41d4-a716-446655440000
Method: POST
URI: /api/transactions

=== FRAUD DETECTION STARTED ===
Transaction ID: TXN-002
Customer ID: CUST-002

Agent Results:
- RiskAgent: 35 points (High amount: $150,000)
- GeoAgent: 30 points (High-risk country: RU)
- DeviceAgent: 65 points (Suspicious device)
- AMLAgent: 10 points (Crypto merchant)
- BehaviorAgent: 35 points (Unusual pattern)

Final Fraud Score: 85/100
Decision: BLOCK
Reason: High fraud score detected

=== FRAUD DETECTION COMPLETED ===
Duration: 245ms
```

---

## 📊 Viewing Results

### 1. Application Logs (Console)
Watch the terminal where you ran `mvn spring-boot:run` for real-time fraud detection output.

### 2. Log Files (After First Request)
```
logs/
├── fraud-platform.log      # All application logs
├── api-requests.log        # API request/response tracking
├── fraud-detection.log     # Fraud agent results
├── kafka.log               # Kafka events
└── error.log               # Errors only
```

**View logs:**
```bash
# Tail all logs
tail -f logs/fraud-platform.log

# View API requests
tail -f logs/api-requests.log

# View fraud detection
tail -f logs/fraud-detection.log
```

### 3. H2 Console (H2 Profile Only)
1. Open: `http://localhost:8080/h2-console`
2. JDBC URL: `jdbc:h2:mem:frauddb`
3. Username: `sa`
4. Password: (leave blank)
5. Click "Connect"

**Query transactions:**
```sql
SELECT * FROM TRANSACTIONS ORDER BY CREATED_AT DESC;
```

### 4. PostgreSQL (Docker Profile)
```bash
# Connect to database
docker exec -it fraud-postgres psql -U frauduser -d frauddb

# Query transactions
SELECT txn_id, customer_id, amount, merchant, fraud_score, fraud_decision, status 
FROM transactions 
ORDER BY created_at DESC 
LIMIT 10;

# Exit
\q
```

---

## 🛑 Stopping the Application

### Stop Spring Boot Application

**Option 1: Graceful Shutdown**
- Press `Ctrl+C` in the terminal where it's running

**Option 2: Force Kill (if terminal not accessible)**
```powershell
# Windows PowerShell
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
```

```bash
# Linux/Mac
pkill -f "spring-boot:run"
```

### Stop Docker Containers

```bash
# Stop and remove all containers
docker-compose -f docker-compose-dev.yml down

# Verify containers are stopped
docker ps
```

### Complete Cleanup

```bash
# Stop everything
docker-compose -f docker-compose-dev.yml down
```

```powershell
# Windows: Kill Java processes
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
```

```bash
# Linux/Mac: Kill Java processes
pkill -f "spring-boot:run"
```

**Verify nothing is running:**
```bash
docker ps
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac
```

---

## 🔍 Troubleshooting

### Issue: Port 8080 already in use
**Solution:** Kill the process using port 8080
```powershell
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

```bash
# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Issue: Docker containers won't start
**Solution:** Check Docker Desktop is running
```bash
docker --version
docker ps
```

If Docker Desktop is not running, start it and wait for it to fully initialize.

### Issue: Kafka connection errors (with H2 profile)
**Expected Behavior:** This is normal when using H2 profile without Docker. The API will still work, but fraud detection won't process via Kafka.

**Solution:** Either:
1. Ignore the errors (API still works for basic testing)
2. Start Docker containers for full Kafka-based fraud detection

### Issue: Maven build fails
**Solution:** Clean and rebuild
```bash
mvn clean install -DskipTests
```

### Issue: Application won't stop
**Solution:** Force kill all Java processes
```powershell
# Windows
Get-Process java | Stop-Process -Force
```

```bash
# Linux/Mac
pkill -9 java
```

### Issue: Database connection errors
**Solution:** Verify database is running
```bash
# For Docker
docker ps | grep postgres

# For H2
# Check application logs for H2 startup messages
```

### Issue: Logs not appearing
**Solution:** Check logs directory exists and has write permissions
```bash
# Create logs directory if missing
mkdir logs

# Check permissions (Linux/Mac)
chmod 755 logs
```

---

## 📝 Test Scenarios

### Scenario 1: Low-Risk Transaction ✅
```json
{
  "txnId": "TXN-LOW-001",
  "customerId": "CUST-001",
  "amount": 5000.00,
  "merchant": "LocalGrocery",
  "country": "US",
  "deviceId": "DEV-001",
  "paymentType": "UPI"
}
```
- **Expected Score:** 15-25
- **Expected Decision:** APPROVE
- **Reason:** Low amount, trusted country, known merchant

### Scenario 2: Medium-Risk Transaction ⚠️
```json
{
  "txnId": "TXN-MED-001",
  "customerId": "CUST-002",
  "amount": 75000.00,
  "merchant": "OnlineRetailer",
  "country": "IN",
  "deviceId": "DEV-NEW-001",
  "paymentType": "CARD"
}
```
- **Expected Score:** 50-65
- **Expected Decision:** OTP
- **Reason:** Moderate amount, new device, online merchant

### Scenario 3: High-Risk Transaction 🚫
```json
{
  "txnId": "TXN-HIGH-001",
  "customerId": "CUST-003",
  "amount": 150000.00,
  "merchant": "CryptoExchange",
  "country": "RU",
  "deviceId": "DEV-SUSPICIOUS-001",
  "paymentType": "CARD"
}
```
- **Expected Score:** 85-95
- **Expected Decision:** BLOCK
- **Reason:** High amount, high-risk country, crypto merchant, suspicious device

---

## 🎯 Complete Testing Workflow

### 1. Start Services
```bash
# Option A: Full setup with Docker
docker-compose -f docker-compose-dev.yml up -d
mvn spring-boot:run

# Option B: Quick testing with H2
$env:SPRING_PROFILES_ACTIVE="h2"; mvn spring-boot:run  # Windows
SPRING_PROFILES_ACTIVE=h2 mvn spring-boot:run          # Linux/Mac
```

### 2. Wait for Startup
Look for: `Started FraudPlatformApplication in X seconds`

### 3. Run Tests
Execute PowerShell or curl commands from the "Testing the Application" section above.

### 4. Check Results
- **Console logs:** Real-time fraud detection output
- **Log files:** `logs/fraud-detection.log` for detailed agent results
- **Database:** Query H2 console or PostgreSQL for transaction records

### 5. Stop Services
```bash
# Stop application (Ctrl+C or kill process)
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force  # Windows
pkill -f "spring-boot:run"                                             # Linux/Mac

# Stop Docker
docker-compose -f docker-compose-dev.yml down
```

---

## 🔗 Related Documentation

### Architecture & Planning
- [`ARCHITECTURE.md`](../architecture/ARCHITECTURE.md) - Complete system architecture
- [`HACKATHON_ARCHITECTURE.md`](../architecture/HACKATHON_ARCHITECTURE.md) - Simplified hackathon architecture
- [`IMPLEMENTATION_PLAN.md`](../development/IMPLEMENTATION_PLAN.md) - Development roadmap

### Development Guides
- [`AGENTS.md`](../../AGENTS.md) - AI assistant guidance for development
- [`LOGGING_GUIDE.md`](LOGGING_GUIDE.md) - End-to-end request tracking and monitoring
- [`DATABASE_SCHEMA.md`](../technical/DATABASE_SCHEMA.md) - Database schema and queries

### Technical Documentation
- [`DECISION_SERVICE_SAMPLE_OUTPUT.md`](../technical/DECISION_SERVICE_SAMPLE_OUTPUT.md) - Sample fraud decisions
- [`EXPLAINABILITY_SAMPLE_OUTPUT.md`](../technical/EXPLAINABILITY_SAMPLE_OUTPUT.md) - Explainable AI output
- [`PROMPTS.md`](../development/PROMPTS.md) - Development prompts and history

---

## 📞 Support

### Common Commands Reference

**Start Application:**
```bash
mvn spring-boot:run                                    # Default (requires Docker)
$env:SPRING_PROFILES_ACTIVE="h2"; mvn spring-boot:run  # H2 mode (Windows)
SPRING_PROFILES_ACTIVE=h2 mvn spring-boot:run          # H2 mode (Linux/Mac)
```

**Check Status:**
```bash
docker ps                                              # Docker containers
netstat -ano | findstr :8080                          # Port 8080 (Windows)
lsof -i :8080                                         # Port 8080 (Linux/Mac)
Get-Process java                                      # Java processes (Windows)
ps aux | grep java                                    # Java processes (Linux/Mac)
```

**View Logs:**
```bash
tail -f logs/fraud-platform.log                       # All logs
tail -f logs/api-requests.log                         # API requests
tail -f logs/fraud-detection.log                      # Fraud detection
tail -f logs/error.log                                # Errors only
```

**Stop Services:**
```bash
docker-compose -f docker-compose-dev.yml down         # Stop Docker
Get-Process java | Stop-Process -Force                # Kill Java (Windows)
pkill -f "spring-boot:run"                            # Kill Java (Linux/Mac)
```

---

## Made with Bob

This fraud investigation platform was built using Bob AI assistant.