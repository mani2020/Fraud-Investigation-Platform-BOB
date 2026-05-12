# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Overview
Enterprise AI-Powered Agentic Fraud Investigation Platform - Enterprise-grade real-time fraud investigation platform for banking payments with AI-powered fraud detection agents.

## Technology Stack
- **Backend**: Java 17, Spring Boot
- **Messaging**: Kafka (event streaming)
- **Database**: PostgreSQL
- **Frontend**: React with IBM Carbon Design System
- **Architecture**: Modular monolith (single Spring Boot app)

## Key Architecture Decisions
- **Fraud Agents**: Implemented as Java service classes (NOT ICA agents)
- **Event Streaming**: Kafka for real-time payment processing
- **No Infrastructure**: No CI/CD, Kubernetes, Terraform, or microservices
- **Hackathon-Optimized**: Single deployable unit, simplified architecture
- **AI Features**: Explainable AI and fraud scoring algorithms

## Build/Test/Lint Commands

### Backend (Spring Boot)
```bash
# Start infrastructure (Kafka + PostgreSQL)
docker-compose -f docker-compose-dev.yml up -d

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Build
mvn clean package
```

### Frontend (React)
```bash
cd frontend
npm install
npm run dev        # Development server
npm run build      # Production build
npm test           # Run tests
```

## Code Style & Conventions
- Java 17 features and syntax
- Spring Boot best practices
- Kafka event-driven patterns
- React with IBM Carbon components

## Architecture & Patterns
- **Modular Monolith**: Single Spring Boot application with clear package boundaries
- **Event-Driven**: Kafka for async processing within the monolith
- **Package Structure**: controller → orchestrator → agents → service → repository → entity
- Service-based fraud detection agents (5 agents: Rule, ML, Velocity, Geo, Behavior)
- Real-time fraud scoring engine with explainable AI
- React dashboard with IBM Carbon Design System

## Testing
- **Unit Tests**: JUnit 5 for Java, Jest for React
- **Integration Tests**: TestContainers for Kafka and PostgreSQL
- **Test Location**: Same package as source files

## Critical Notes
- **Single Application**: All services in one Spring Boot app (modular monolith)
- **Fraud Agents**: Java service classes implementing FraudAgent interface (NOT ICA agents)
- **Kafka**: Event streaming within monolith for async processing
- **No Microservices**: Simplified architecture for hackathon/demo
- **Package Structure**: Follow com.fraud.platform.{controller,orchestrator,agents,service,repository,entity}
- **Demo-Ready**: Focus on visual impact and core fraud detection features

## Project Structure
```
src/main/java/com/fraud/platform/
├── controller/       # REST & WebSocket endpoints
├── orchestrator/     # Fraud orchestration logic
├── agents/          # 5 fraud detection agents
├── service/         # Business logic services
├── kafka/           # Kafka producer/consumer
├── repository/      # JPA repositories
├── entity/          # JPA entities
├── model/           # DTOs
├── config/          # Configuration classes
└── util/            # Utility classes
```