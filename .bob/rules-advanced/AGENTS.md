# Advanced Mode Rules - Fraud Investigation Platform

## Technology Stack
- Java 17 with Spring Boot
- Kafka for event streaming
- PostgreSQL database
- React with IBM Carbon Design System

## Critical Architecture Rules
- Fraud agents MUST be Java service classes (NOT ICA agents)
- All payment processing MUST use Kafka event streams
- No infrastructure code (CI/CD, Kubernetes, Terraform)

## Code Patterns (To be discovered)
(Update as non-obvious patterns emerge during development)

## Tool Access
- Full access to MCP and Browser tools
- Use for testing React UI with IBM Carbon components
- Use for debugging Kafka event flows