# Plan Mode Rules - Fraud Investigation Platform

## Project Overview
Enterprise-grade real-time fraud investigation platform for banking payments.

## Technology Stack
- Java 17 with Spring Boot
- Kafka for event streaming
- PostgreSQL database
- React with IBM Carbon Design System

## Architecture Constraints
- Fraud agents MUST be Java service classes (NOT ICA agents)
- Event-driven architecture with Kafka for real-time processing
- No infrastructure code (CI/CD, Kubernetes, Terraform)
- PostgreSQL for persistent storage and audit trails

## Key Features to Plan
- Real-time fraud detection agents (Java services)
- Kafka event streaming for payment processing
- Explainable AI decision tracking
- Fraud scoring algorithms
- React dashboard with IBM Carbon Design System

## Architecture Patterns (To be discovered)
(Update as non-obvious architectural patterns emerge during development)

## Planning Considerations
- Service boundaries for fraud detection agents
- Kafka topic design for event streaming
- Database schema for fraud cases and audit trails
- React component architecture with IBM Carbon
- API design between backend and frontend