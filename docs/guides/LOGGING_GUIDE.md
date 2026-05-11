# Logging Guide - Fraud Investigation Platform

## Overview

This platform implements industry-standard logging practices for end-to-end API request tracking, fraud detection monitoring, and system diagnostics.

## Logging Architecture

### Components

1. **Logback Configuration** (`logback-spring.xml`)
   - Structured logging with multiple appenders
   - Separate log files for different concerns
   - Rolling file policies with size and time-based rotation
   - Async appenders for performance

2. **Logging Interceptor** (`LoggingInterceptor.java`)
   - Tracks all API requests end-to-end
   - Generates unique request IDs for correlation
   - Measures request duration
   - Uses MDC (Mapped Diagnostic Context) for thread-safe logging

3. **Application Configuration** (`application.yml`)
   - Log levels per package
   - File rotation settings
   - Console and file output patterns

---

## Log Files

All log files are stored in the `./logs` directory:

### 1. `fraud-platform.log`
**Purpose:** Main application log  
**Contains:** All application events, errors, and general information  
**Rotation:** Daily, max 10MB per file, 30 days retention

**Example:**
```
2026-05-10 17:30:15.123 [http-nio-8080-exec-1] INFO  c.f.p.controller.TransactionController - Transaction created: TXN-001
2026-05-10 17:30:15.456 [http-nio-8080-exec-1] INFO  c.f.p.service.TransactionService - Processing transaction for customer: CUST-001
```

### 2. `api-requests.log`
**Purpose:** API request/response tracking  
**Contains:** HTTP method, URI, status codes, duration, request IDs  
**Rotation:** Daily, max 10MB per file, 30 days retention

**Example:**
```
2026-05-10 17:30:15.100 [http-nio-8080-exec-1] INFO  c.f.p.config.LoggingInterceptor - === INCOMING REQUEST ===
2026-05-10 17:30:15.101 [http-nio-8080-exec-1] INFO  c.f.p.config.LoggingInterceptor - Request ID: 550e8400-e29b-41d4-a716-446655440000
2026-05-10 17:30:15.102 [http-nio-8080-exec-1] INFO  c.f.p.config.LoggingInterceptor - Method: POST
2026-05-10 17:30:15.103 [http-nio-8080-exec-1] INFO  c.f.p.config.LoggingInterceptor - URI: /api/transactions
2026-05-10 17:30:15.104 [http-nio-8080-exec-1] INFO  c.f.p.config.LoggingInterceptor - Remote Address: 127.0.0.1
2026-05-10 17:30:15.500 [http-nio-8080-exec-1] INFO  c.f.p.config.LoggingInterceptor - === OUTGOING RESPONSE ===
2026-05-10 17:30:15.501 [http-nio-8080-exec-1] INFO  c.f.p.config.LoggingInterceptor - Status Code: 200
2026-05-10 17:30:15.502 [http-nio-8080-exec-1] INFO  c.f.p.config.LoggingInterceptor - Duration: 400 ms
```

### 3. `fraud-detection.log`
**Purpose:** Fraud detection processing  
**Contains:** Agent execution, scoring, decisions  
**Rotation:** Daily, max 10MB per file, 30 days retention

**Example:**
```
2026-05-10 17:30:15.200 [kafka-consumer-1] INFO  c.f.p.orchestrator.FraudOrchestratorService - === FRAUD DETECTION STARTED ===
2026-05-10 17:30:15.201 [kafka-consumer-1] INFO  c.f.p.orchestrator.FraudOrchestratorService - Transaction ID: TXN-001
2026-05-10 17:30:15.250 [kafka-consumer-1] INFO  c.f.p.agents.RiskAgent - RiskAgent score: 35
2026-05-10 17:30:15.251 [kafka-consumer-1] INFO  c.f.p.agents.GeoAgent - GeoAgent score: 30
2026-05-10 17:30:15.252 [kafka-consumer-1] INFO  c.f.p.agents.DeviceAgent - DeviceAgent score: 65
2026-05-10 17:30:15.300 [kafka-consumer-1] INFO  c.f.p.orchestrator.FraudOrchestratorService - Final Fraud Score: 19/100
2026-05-10 17:30:15.301 [kafka-consumer-1] INFO  c.f.p.orchestrator.FraudOrchestratorService - Decision: APPROVE
```

### 4. `kafka.log`
**Purpose:** Kafka message processing  
**Contains:** Producer/consumer events, topic operations  
**Rotation:** Daily, max 10MB per file, 7 days retention

**Example:**
```
2026-05-10 17:30:15.150 [http-nio-8080-exec-1] INFO  c.f.p.kafka.KafkaProducerService - Publishing transaction event: TXN-001
2026-05-10 17:30:15.180 [kafka-consumer-1] INFO  c.f.p.kafka.KafkaConsumerService - Received transaction event: TXN-001
```

### 5. `error.log`
**Purpose:** Error tracking  
**Contains:** All ERROR level logs and exceptions  
**Rotation:** Daily, max 10MB per file, 30 days retention

**Example:**
```
2026-05-10 17:30:15.999 [http-nio-8080-exec-1] ERROR c.f.p.controller.TransactionController - Failed to process transaction
java.lang.RuntimeException: Database connection failed
    at com.fraud.platform.service.TransactionService.save(TransactionService.java:45)
    ...
```

---

## Request Correlation

### Request ID
Every API request receives a unique Request ID (UUID) for end-to-end tracking:

**Response Header:**
```
X-Request-ID: 550e8400-e29b-41d4-a716-446655440000
```

**Usage:**
1. Client receives Request ID in response header
2. Use Request ID to search logs for complete request flow
3. Track request across multiple services/components

**Example Search:**
```bash
# Find all logs for a specific request
grep "550e8400-e29b-41d4-a716-446655440000" ./logs/*.log
```

---

## Log Levels

### Application Packages
- `com.fraud.platform.controller`: INFO
- `com.fraud.platform.orchestrator`: INFO
- `com.fraud.platform.agents`: INFO
- `com.fraud.platform.service`: INFO
- `com.fraud.platform.kafka`: INFO

### Framework Packages
- `org.springframework`: INFO
- `org.springframework.kafka`: WARN
- `org.apache.kafka`: WARN
- `org.hibernate`: WARN
- `org.hibernate.SQL`: DEBUG (for SQL queries)

### Changing Log Levels
Edit `application.yml`:
```yaml
logging:
  level:
    com.fraud.platform.controller: DEBUG  # Change to DEBUG for more details
```

---

## Performance Monitoring

### Slow Request Detection
Requests taking longer than 1 second are automatically flagged:

```
2026-05-10 17:30:16.500 [http-nio-8080-exec-1] WARN  c.f.p.config.LoggingInterceptor - SLOW REQUEST DETECTED - Duration: 1250 ms, URI: /api/transactions
```

### Metrics Tracked
- Request duration (milliseconds)
- HTTP status codes
- Request method and URI
- Remote client address

---

## Searching Logs

### By Request ID
```bash
# PowerShell
Select-String -Path ".\logs\*.log" -Pattern "550e8400-e29b-41d4-a716-446655440000"

# Linux/Mac
grep -r "550e8400-e29b-41d4-a716-446655440000" ./logs/
```

### By Transaction ID
```bash
# PowerShell
Select-String -Path ".\logs\fraud-detection.log" -Pattern "TXN-001"

# Linux/Mac
grep "TXN-001" ./logs/fraud-detection.log
```

### By Error Level
```bash
# PowerShell
Select-String -Path ".\logs\error.log" -Pattern "ERROR"

# Linux/Mac
grep "ERROR" ./logs/error.log
```

### By Time Range
```bash
# PowerShell
Select-String -Path ".\logs\api-requests.log" -Pattern "2026-05-10 17:3"

# Linux/Mac
grep "2026-05-10 17:3" ./logs/api-requests.log
```

### Slow Requests
```bash
# PowerShell
Select-String -Path ".\logs\api-requests.log" -Pattern "SLOW REQUEST"

# Linux/Mac
grep "SLOW REQUEST" ./logs/api-requests.log
```

---

## Log Rotation

### Configuration
- **Max File Size:** 10MB
- **Retention Period:** 30 days (7 days for Kafka logs)
- **Total Size Cap:** 1GB
- **Naming Pattern:** `{filename}-{date}.{index}.log`

### Example Rotated Files
```
logs/
├── fraud-platform.log              # Current
├── fraud-platform-2026-05-09.0.log # Yesterday
├── fraud-platform-2026-05-09.1.log # Yesterday (rotated by size)
├── fraud-platform-2026-05-08.0.log # 2 days ago
└── ...
```

---

## Best Practices

### 1. Always Include Request ID
When reporting issues, include the Request ID from the response header.

### 2. Check Multiple Log Files
For complete troubleshooting:
1. Start with `api-requests.log` (find Request ID)
2. Check `fraud-detection.log` (fraud processing)
3. Review `error.log` (if errors occurred)
4. Examine `kafka.log` (message flow)

### 3. Monitor Slow Requests
Regularly check for SLOW REQUEST warnings to identify performance issues.

### 4. Archive Old Logs
Logs older than retention period are automatically deleted. Archive important logs before deletion.

---

## Troubleshooting

### Issue: No logs generated
**Solution:** Check logs directory exists and has write permissions
```bash
mkdir -p ./logs
chmod 755 ./logs
```

### Issue: Logs not rotating
**Solution:** Check disk space and file permissions
```bash
df -h
ls -la ./logs/
```

### Issue: Too many logs
**Solution:** Adjust log levels in `application.yml`
```yaml
logging:
  level:
    root: WARN  # Reduce from INFO to WARN
```

### Issue: Missing request correlation
**Solution:** Ensure LoggingInterceptor is registered in WebMvcConfig

---

## Integration with Monitoring Tools

### ELK Stack (Elasticsearch, Logstash, Kibana)
Logs are formatted for easy ingestion:
```json
{
  "timestamp": "2026-05-10T17:30:15.123Z",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.fraud.platform.controller.TransactionController",
  "message": "Transaction created: TXN-001",
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Splunk
Use the file appenders to forward logs to Splunk:
```bash
# Configure Splunk Universal Forwarder to monitor ./logs directory
```

### Grafana Loki
Logs can be scraped by Promtail for Loki ingestion.

---

## Made with Bob

This logging infrastructure follows industry best practices for:
- Request correlation
- Performance monitoring
- Error tracking
- Audit trails
- Compliance requirements