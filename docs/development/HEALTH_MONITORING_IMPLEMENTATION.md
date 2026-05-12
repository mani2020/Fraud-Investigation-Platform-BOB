# Health Monitoring System Implementation

## Overview
Implemented real-time health monitoring system to replace hardcoded mock data in the System Health dashboard.

## Problem
The System Health dashboard was showing static/mock data:
- All services always showed as "Active/Connected"
- No actual health checks were performed
- Misleading status information

## Solution
Created a comprehensive health monitoring system with:
1. Backend health check service
2. REST API endpoints
3. Real-time frontend integration
4. Visual status indicators

---

## Backend Implementation

### 1. Health Model (`SystemHealth.java`)
**Location**: `src/main/java/com/fraud/platform/model/SystemHealth.java`

Defines the health status structure:
```java
public class SystemHealth {
    private String status; // HEALTHY, DEGRADED, DOWN
    private LocalDateTime timestamp;
    private KafkaHealth kafka;
    private DatabaseHealth database;
    private AgentsHealth agents;
    private ApiHealth api;
}
```

**Component Health Models**:
- `KafkaHealth` - Kafka connection status
- `DatabaseHealth` - Database connectivity and performance
- `AgentsHealth` - Fraud detection agents status
- `ApiHealth` - API Gateway operational status

### 2. Health Check Service (`HealthCheckService.java`)
**Location**: `src/main/java/com/fraud/platform/service/HealthCheckService.java`

**Key Features**:
- Real-time Kafka connection monitoring
- Database health checks with response time tracking
- Agent status tracking (5 fraud detection agents)
- Overall system status determination

**Health Check Methods**:

#### Kafka Health Check
```java
private SystemHealth.KafkaHealth checkKafkaHealth() {
    // Tests Kafka connection via metrics
    // Returns: CONNECTED, ERROR
}
```

#### Database Health Check
```java
private SystemHealth.DatabaseHealth checkDatabaseHealth() {
    // Tests database connection
    // Measures response time
    // Returns: HEALTHY (<100ms), SLOW (100-500ms), DOWN (>500ms or error)
}
```

#### Agents Health Check
```java
private SystemHealth.AgentsHealth checkAgentsHealth() {
    // Monitors 5 fraud detection agents:
    // - RiskAgent
    // - GeoAgent
    // - DeviceAgent
    // - AMLAgent
    // - BehaviorAgent
    // Returns: ALL_ACTIVE, PARTIAL, DOWN
}
```

#### API Health Check
```java
private SystemHealth.ApiHealth checkApiHealth() {
    // Tracks API uptime
    // Returns: OPERATIONAL, DEGRADED, DOWN
}
```

### 3. Health Controller (`HealthController.java`)
**Location**: `src/main/java/com/fraud/platform/controller/HealthController.java`

**Endpoints**:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/health` | GET | Get comprehensive system health |
| `/api/health/ping` | GET | Simple availability check |
| `/api/health/status` | GET | Alias for main health endpoint |

**Example Response**:
```json
{
  "status": "HEALTHY",
  "timestamp": "2026-05-12T09:00:00",
  "kafka": {
    "status": "CONNECTED",
    "message": "Kafka stream is operational",
    "lastMessageTime": 1715508000000
  },
  "database": {
    "status": "HEALTHY",
    "message": "Database responding in 45ms",
    "responseTimeMs": 45,
    "transactionCount": 20
  },
  "agents": {
    "status": "ALL_ACTIVE",
    "activeCount": 5,
    "totalCount": 5,
    "agents": [
      {
        "name": "RiskAgent",
        "status": "ACTIVE",
        "successRate": 92,
        "lastExecutionTime": 1715507995000
      }
      // ... other agents
    ]
  },
  "api": {
    "status": "OPERATIONAL",
    "message": "API Gateway is operational",
    "uptime": 3600
  }
}
```

---

## Frontend Implementation

### 1. API Configuration
**Location**: `frontend/src/config/api.js`

Added health endpoints:
```javascript
HEALTH: `${API_BASE_URL}/api/health`,
HEALTH_PING: `${API_BASE_URL}/api/health/ping`,
```

### 2. Dashboard Component Updates
**Location**: `frontend/src/pages/Dashboard.jsx`

**Changes**:
1. Added `systemHealth` state
2. Created `fetchSystemHealth()` function
3. Updated polling to fetch health every 10 seconds
4. Replaced hardcoded status with dynamic data

**Health Fetching**:
```javascript
const fetchSystemHealth = async () => {
  try {
    const response = await axios.get(API_ENDPOINTS.HEALTH);
    setSystemHealth(response.data);
  } catch (error) {
    // Set error state if health check fails
    setSystemHealth({
      status: 'DOWN',
      kafka: { status: 'ERROR' },
      database: { status: 'DOWN' },
      agents: { status: 'DOWN', activeCount: 0, totalCount: 5 },
      api: { status: 'DOWN' }
    });
  }
};
```

**Dynamic Status Display**:
```jsx
<div className="status-item">
  <div className={`status-indicator ${
    systemHealth.kafka?.status === 'CONNECTED' ? 'active' : 'error'
  }`}></div>
  <span className="status-label">Kafka Stream</span>
  <span className="status-value">{systemHealth.kafka?.status}</span>
</div>
```

### 3. Styling Updates
**Location**: `frontend/src/pages/Dashboard.scss`

Added status indicator states:
```scss
.status-indicator {
  &.active {
    background: $status-success;
    box-shadow: $glow-success;
  }
  
  &.warning {
    background: $status-warning;
    box-shadow: 0 0 10px rgba($status-warning, 0.5);
  }
  
  &.error {
    background: $status-danger;
    box-shadow: 0 0 10px rgba($status-danger, 0.5);
  }
}
```

---

## Status Indicators

### Visual States

| State | Color | Condition | Example |
|-------|-------|-----------|---------|
| **Active** (Green) | Success | Component fully operational | Kafka: CONNECTED |
| **Warning** (Yellow) | Warning | Component degraded | Database: SLOW |
| **Error** (Red) | Danger | Component down/error | Kafka: ERROR |

### Component Status Values

#### Kafka
- `CONNECTED` → Green (Active)
- `DISCONNECTED` → Red (Error)
- `ERROR` → Red (Error)

#### Database
- `HEALTHY` → Green (Active)
- `SLOW` → Yellow (Warning)
- `DOWN` → Red (Error)

#### Agents
- `ALL_ACTIVE` → Green (Active)
- `PARTIAL` → Yellow (Warning)
- `DOWN` → Red (Error)

#### API
- `OPERATIONAL` → Green (Active)
- `DEGRADED` → Yellow (Warning)
- `DOWN` → Red (Error)

---

## Testing

### Backend Testing

1. **Test Health Endpoint**:
```bash
curl http://localhost:8080/api/health
```

2. **Test Ping Endpoint**:
```bash
curl http://localhost:8080/api/health/ping
```

3. **Expected Response**:
- Status code: 200 OK
- JSON response with all component statuses

### Frontend Testing

1. **Start Application**:
```bash
# Backend
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=default" "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"

# Frontend
cd frontend
npm run dev
```

2. **Verify Dashboard**:
- Navigate to Dashboard
- Check System Health section
- Verify status indicators show correct colors
- Confirm status values update every 10 seconds

3. **Test Error Handling**:
- Stop Spring Boot
- Verify frontend shows error states (red indicators)
- Restart Spring Boot
- Verify status returns to normal

---

## Monitoring & Maintenance

### Health Check Frequency
- **Frontend Polling**: Every 10 seconds
- **Backend Checks**: On-demand (per request)

### Performance Considerations
- Database health check timeout: 5 seconds
- Kafka metrics check: Lightweight operation
- Agent status: Simulated (can be enhanced with real tracking)

### Future Enhancements

1. **Agent Status Tracking**:
   - Implement real agent execution tracking
   - Track success/failure rates
   - Monitor agent response times

2. **Historical Data**:
   - Store health check history
   - Create health trends dashboard
   - Alert on degraded performance

3. **Alerting**:
   - Email notifications for DOWN status
   - Slack integration for critical alerts
   - Configurable alert thresholds

4. **Detailed Metrics**:
   - Kafka consumer lag
   - Database connection pool stats
   - API response time percentiles

---

## Troubleshooting

### Issue: All Components Show as DOWN

**Cause**: Backend not running or health endpoint not accessible

**Solution**:
1. Verify Spring Boot is running
2. Check logs for errors
3. Test health endpoint directly: `curl http://localhost:8080/api/health`

### Issue: Database Shows as SLOW

**Cause**: Database response time > 100ms

**Solution**:
1. Check database connection
2. Verify PostgreSQL is running
3. Check for slow queries
4. Consider connection pool tuning

### Issue: Kafka Shows as ERROR

**Cause**: Kafka connection issues

**Solution**:
1. Verify Kafka is running: `docker ps`
2. Check Kafka logs
3. Verify Kafka configuration in `application.yml`
4. Test Kafka connection manually

---

## Files Modified/Created

### Backend
- ✅ Created: `src/main/java/com/fraud/platform/model/SystemHealth.java`
- ✅ Created: `src/main/java/com/fraud/platform/service/HealthCheckService.java`
- ✅ Created: `src/main/java/com/fraud/platform/controller/HealthController.java`

### Frontend
- ✅ Modified: `frontend/src/config/api.js`
- ✅ Modified: `frontend/src/pages/Dashboard.jsx`
- ✅ Modified: `frontend/src/pages/Dashboard.scss`

### Documentation
- ✅ Created: `docs/development/HEALTH_MONITORING_IMPLEMENTATION.md`

---

*Made with Bob*