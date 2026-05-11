# Fraud Investigation Platform - Startup Guide

## Quick Start Instructions

### Prerequisites
- Java 17 or higher
- Node.js 18+ and npm
- Docker Desktop (optional, for full Kafka support)
- Maven 3.6+

---

## Option 1: Quick Start (Without Kafka - H2 Database)

### Step 1: Start Backend (H2 Profile)

Open a terminal and run:

```powershell
# Navigate to project directory
cd "c:\Users\ManikandanP\OneDrive - IBM\BOB Projects\fraud-investigation-platform"

# Start backend with H2 in-memory database
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=h2"
```

**Expected Output:**
```
Started FraudPlatformApplication in X seconds (process running for Y)
Tomcat started on port 8080 (http) with context path ''
```

**Note:** You may see Kafka connection warnings - these are expected and can be ignored when using H2 profile.

**Backend will be available at:** `http://localhost:8080`

---

### Step 2: Start Frontend

Open a **NEW terminal** (keep backend running) and run:

```powershell
# Navigate to frontend directory
cd "c:\Users\ManikandanP\OneDrive - IBM\BOB Projects\fraud-investigation-platform\frontend"

# If you get PowerShell execution policy error, run this first:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

**Expected Output:**
```
VITE v5.x.x  ready in XXX ms

➜  Local:   http://localhost:5174/
➜  Network: use --host to expose
```

**Frontend will be available at:** `http://localhost:5174`

---

## Option 2: Full Setup (With Kafka + PostgreSQL)

### Step 1: Start Infrastructure

```powershell
# Start Docker Desktop first, then run:
docker-compose -f docker-compose-dev.yml up -d

# Verify services are running:
docker-compose -f docker-compose-dev.yml ps
```

**Expected Services:**
- PostgreSQL on port 5432
- Kafka on port 9092
- Zookeeper on port 2181

---

### Step 2: Start Backend (Default Profile)

```powershell
# Start backend with PostgreSQL and Kafka
mvn spring-boot:run
```

**Backend will be available at:** `http://localhost:8080`

---

### Step 3: Start Frontend

```powershell
cd frontend
npm install  # First time only
npm run dev
```

**Frontend will be available at:** `http://localhost:5174`

---

## Troubleshooting

### PowerShell Execution Policy Error

If you see:
```
npm : File C:\Program Files\nodejs\npm.ps1 cannot be loaded because running scripts is disabled
```

**Solution:**
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

### Backend Kafka Connection Warnings

If you see repeated warnings:
```
Connection to node -1 (localhost/127.0.0.1:9092) could not be established
```

**Solutions:**
1. **Using H2 Profile:** These warnings are expected and can be ignored. The app will work without Kafka.
2. **Want full Kafka support:** Start Docker Desktop and run `docker-compose -f docker-compose-dev.yml up -d`

---

### Port Already in Use

If port 8080 or 5174 is already in use:

**Backend (8080):**
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

**Frontend (5174):**
```powershell
# Find process using port 5174
netstat -ano | findstr :5174

# Kill the process
taskkill /PID <PID> /F
```

---

## Verify Installation

### Backend Health Check

Open browser or use curl:
```powershell
# Check if backend is running
curl http://localhost:8080/api/transactions

# Expected: Empty array [] or list of transactions
```

### Frontend Access

Open browser:
```
http://localhost:5174
```

You should see the Fraud Investigation Platform dashboard.

---

## API Endpoints

Once backend is running, you can test these endpoints:

### Transaction APIs
- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/{txnId}` - Get transaction by ID
- `POST /api/transactions` - Create new transaction
- `GET /api/transactions/customer/{customerId}` - Get customer transactions

### Fraud Alert APIs
- `GET /api/fraud-alerts` - Get all alerts
- `GET /api/fraud-alerts/recent` - Get recent alerts (last 100)
- `GET /api/fraud-alerts/severity/{severity}` - Get alerts by severity
- `GET /api/fraud-alerts/customer/{customerId}` - Get customer alerts
- `GET /api/fraud-alerts/unread` - Get unread alerts
- `PUT /api/fraud-alerts/{id}/read` - Mark alert as read
- `DELETE /api/fraud-alerts/{id}` - Delete alert
- `GET /api/fraud-alerts/stats` - Get alert statistics

---

## Sample API Request

### Create a High-Risk Transaction

```powershell
# Using PowerShell
$body = @{
    txnId = "TXN-2026-001"
    customerId = "CUST-12345"
    amount = 50000.00
    merchant = "Unknown Merchant"
    country = "NG"
    deviceId = "unknown-device-123"
    paymentType = "WIRE_TRANSFER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/transactions" -Method Post -Body $body -ContentType "application/json"
```

---

## Development Workflow

### 1. Backend Development
```powershell
# Make code changes
# Backend auto-reloads with Spring Boot DevTools
# Check logs in terminal
```

### 2. Frontend Development
```powershell
# Make code changes
# Vite hot-reloads automatically
# Check browser console for errors
```

### 3. Testing
```powershell
# Backend tests
mvn test

# Frontend tests
cd frontend
npm test
```

---

## Stopping the Application

### Stop Frontend
Press `Ctrl + C` in the frontend terminal

### Stop Backend
Press `Ctrl + C` in the backend terminal

### Stop Docker Services (if running)
```powershell
docker-compose -f docker-compose-dev.yml down
```

---

## Current Status

✅ **Backend:** Running on port 8080 with H2 database
⚠️ **Kafka:** Not required for basic functionality (warnings can be ignored)
⏳ **Frontend:** Ready to start (follow Step 2 above)

---

## Next Steps

1. ✅ Backend is running on `http://localhost:8080`
2. 🔄 Start frontend: `cd frontend && npm run dev`
3. 🌐 Access dashboard: `http://localhost:5174`
4. 🧪 Test API: Create a transaction via POST request
5. 📊 View results in dashboard

---

## Additional Resources

- **Architecture:** See `docs/architecture/ARCHITECTURE.md`
- **API Documentation:** See `docs/technical/FRAUD_FLOW_TECHNICAL.md`
- **Frontend Guide:** See `frontend/README.md`
- **Database Schema:** See `docs/technical/DATABASE_SCHEMA.md`

---

**Made with ❤️ for Fraud Investigation Platform**