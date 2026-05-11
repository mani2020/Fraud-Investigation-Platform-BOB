# Fraud Investigation Platform

Enterprise-grade real-time fraud investigation platform for banking payments built as a hackathon-optimized modular monolith.

## Overview

A single Spring Boot application that provides:
- Real-time fraud detection using 5 specialized agents
- Kafka event streaming for async processing
- Explainable AI for fraud decisions
- React dashboard with IBM Carbon Design System
- Multi-channel support (cards, transfers, mobile payments)

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2**
- **Apache Kafka** - Event streaming
- **PostgreSQL 15** - Database
- **Flyway** - Database migrations
- **Caffeine** - In-memory caching
- **Lombok** - Boilerplate reduction

### Frontend
- **React 18**
- **IBM Carbon Design System**
- **Redux Toolkit**
- **Vite**

## Architecture

### Modular Monolith Structure
```
com.fraud.platform/
├── controller/       # REST & WebSocket endpoints
├── orchestrator/     # Fraud orchestration logic
├── agents/          # 5 fraud detection agents
│   ├── RuleBasedAgent
│   ├── MLScoringAgent
│   ├── VelocityAgent
│   ├── GeoLocationAgent
│   └── BehaviorAgent
├── service/         # Business logic services
├── kafka/           # Kafka producer/consumer
├── repository/      # JPA repositories
├── entity/          # JPA entities
├── model/           # DTOs
├── config/          # Configuration classes
└── util/            # Utility classes
```

### Event Flow
```
<img width="2400" height="1360" alt="image" src="https://github.com/user-attachments/assets/7423606a-102c-4f30-876b-a86c0041da18" />

```

## Quick Start

### Prerequisites
- Java 17
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

### 1. Start Infrastructure

```bash
# Start Kafka and PostgreSQL
docker-compose -f docker-compose-dev.yml up -d

# Verify services are running
docker-compose -f docker-compose-dev.yml ps
```

### 2. Run Backend

```bash
# Build the application
mvn clean install

# Run Spring Boot application
mvn spring-boot:run

# Or run the JAR
java -jar target/fraud-investigation-platform-1.0.0-SNAPSHOT.jar
```

The backend will start on `http://localhost:8080`

### 3. Run Frontend (Coming Soon)

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:5173`

## API Endpoints

### Transaction API
```
POST   /api/transactions              # Submit transaction for fraud check
GET    /api/transactions/{id}         # Get transaction details
GET    /api/transactions              # List transactions
```

### Case Management API
```
GET    /api/cases                     # List fraud cases
GET    /api/cases/{id}                # Get case details
PUT    /api/cases/{id}/assign         # Assign case to analyst
PUT    /api/cases/{id}/resolve        # Resolve case
POST   /api/cases/{id}/notes          # Add note to case
```

### Dashboard API
```
GET    /api/dashboard/metrics         # Real-time metrics
GET    /api/dashboard/alerts          # Recent alerts
GET    /api/dashboard/trends          # Fraud trends
```

### WebSocket
```
ws://localhost:8080/ws/fraud-alerts   # Real-time fraud alerts
```

## Configuration

### Application Configuration
Edit `src/main/resources/application.yml`:

```yaml
fraud:
  agents:
    rule-based:
      high-amount-threshold: 10000
    velocity:
      max-transactions-per-hour: 10
  decision:
    auto-approve-threshold: 30.0
    review-threshold: 70.0
```

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/frauddb
    username: fraud
    password: fraud123
```

### Kafka Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=FraudOrchestratorTest

# Run integration tests
mvn verify
```

### Database Migrations

Flyway migrations are in `src/main/resources/db/migration/`

```bash
# Migrations run automatically on startup
# To run manually:
mvn flyway:migrate
```

### Code Style

- Java 17 features and syntax
- Lombok for boilerplate reduction
- Spring Boot best practices
- Layered architecture pattern

## Project Structure

```
fraud-investigation-platform/
├── src/
│   ├── main/
│   │   ├── java/com/fraud/platform/
│   │   │   ├── FraudPlatformApplication.java
│   │   │   ├── controller/
│   │   │   ├── orchestrator/
│   │   │   ├── agents/
│   │   │   ├── service/
│   │   │   ├── kafka/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── model/
│   │   │   ├── config/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
├── frontend/                    # React dashboard (coming soon)
├── docs/
│   ├── ARCHITECTURE.md
│   ├── HACKATHON_ARCHITECTURE.md
│   ├── HACKATHON_PLAN.md
│   └── IMPLEMENTATION_PLAN.md
├── docker-compose-dev.yml
├── pom.xml
└── README.md
```

## Fraud Detection Agents

### 1. Rule-Based Agent
- Amount threshold checks
- Blacklist validation
- Time-based restrictions
- Duplicate detection

### 2. ML Scoring Agent
- Machine learning-based fraud prediction
- Feature importance calculation
- Fraud probability scoring

### 3. Velocity Agent
- Transaction frequency analysis
- Amount velocity checks
- Geographic velocity detection

### 4. Geo-Location Agent
- Impossible travel detection
- High-risk country checks
- Location consistency validation

### 5. Behavior Agent
- Spending pattern analysis
- Merchant category deviation
- Time-of-day anomalies

## Explainable AI

Every fraud decision includes:
- Overall fraud score (0-100)
- Contributing factors from each agent
- Feature importance breakdown
- Human-readable explanation

Example:
```json
{
  "transaction_id": "TXN123",
  "final_score": 87.5,
  "decision": "REVIEW",
  "explanation": "High fraud risk detected",
  "contributing_factors": [
    {
      "factor": "Transaction amount significantly higher than average",
      "impact": 35.2,
      "agent": "ML_SCORING_AGENT"
    },
    {
      "factor": "Transaction from new geographic location",
      "impact": 28.7,
      "agent": "GEO_LOCATION_AGENT"
    }
  ]
}
```

## Performance Targets

- **Throughput**: 100 TPS (hackathon target)
- **Latency**: < 500ms per transaction
- **Availability**: 99.9% uptime

## Monitoring

### Application Logs
```bash
# View logs
tail -f logs/application.log

# Or use Docker logs
docker-compose -f docker-compose-dev.yml logs -f
```

### Kafka Topics
```bash
# List topics
docker exec -it fraud-kafka kafka-topics --list --bootstrap-server localhost:9092

# View messages
docker exec -it fraud-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic fraud-transactions --from-beginning
```

### Database
```bash
# Connect to PostgreSQL
docker exec -it fraud-postgres psql -U fraud -d frauddb

# View tables
\dt

# Query transactions
SELECT * FROM transactions LIMIT 10;
```

## Troubleshooting

### Kafka Connection Issues
```bash
# Restart Kafka
docker-compose -f docker-compose-dev.yml restart kafka

# Check Kafka logs
docker-compose -f docker-compose-dev.yml logs kafka
```

### Database Connection Issues
```bash
# Restart PostgreSQL
docker-compose -f docker-compose-dev.yml restart postgres

# Check PostgreSQL logs
docker-compose -f docker-compose-dev.yml logs postgres
```

### Application Won't Start
```bash
# Clean and rebuild
mvn clean install

# Check for port conflicts
netstat -ano | findstr :8080
```


## Documentation

### Architecture & Planning
- [Architecture Overview](docs/architecture/ARCHITECTURE.md) - Original microservices design
- [Hackathon Architecture](docs/architecture/HACKATHON_ARCHITECTURE.md) - Simplified modular monolith

### Development Guides
- [AGENTS.md](AGENTS.md) - AI assistant guidance
- [Setup & Testing Guide](docs/guides/SETUP_TESTING.md) - Complete setup and testing instructions
- [Logging Guide](docs/guides/LOGGING_GUIDE.md) - End-to-end request tracking and monitoring

### Technical Documentation
- [Database Schema](docs/technical/DATABASE_SCHEMA.md) - Complete database design
- [Decision Service Output](docs/technical/DECISION_SERVICE_SAMPLE_OUTPUT.md) - Fraud decision examples
- [Explainability Output](docs/technical/EXPLAINABILITY_SAMPLE_OUTPUT.md) - AI explanation examples

### Development History
- [Implementation Plan](docs/development/IMPLEMENTATION_PLAN.md) - 16-week detailed plan
- [Hackathon Plan](docs/development/HACKATHON_PLAN.md) - 5-day sprint plan
- [Code Review Summary](docs/development/CODE_REVIEW_SUMMARY.md) - Quality assurance results
- [Problems Explained](docs/development/PROBLEMS_TAB_EXPLANATION.md) - Common issues and solutions
- [Prompts Used](docs/development/PROMPTS.md) - All Bob prompts for building this platform

## Contributing

This is a hackathon project. Focus on:
- Working features over perfect code
- Visual impact for demo
- Clear explanations of AI decisions
- Smooth demo flow

## License

Proprietary - Internal Use Only

## Contact

For questions or issues, contact the development team.

---

**Made with ❤️ for the Bob-a-thon Hackathon**
