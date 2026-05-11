# Documentation Structure

This directory contains all project documentation organized by category for easy navigation and readability.

## 📁 Folder Structure

```
docs/
├── architecture/          # System design and architecture
├── guides/               # User and developer guides
├── technical/            # Technical specifications and samples
└── development/          # Development history and planning
```

## 📚 Documentation Index

### 🏗️ Architecture (`architecture/`)
High-level system design and architectural decisions.

- **[ARCHITECTURE.md](architecture/ARCHITECTURE.md)** - Complete system architecture (original microservices design)
- **[HACKATHON_ARCHITECTURE.md](architecture/HACKATHON_ARCHITECTURE.md)** - Simplified modular monolith for hackathon

### 📖 Guides (`guides/`)
Step-by-step guides for setup, testing, and operations.

- **[SETUP_TESTING.md](guides/SETUP_TESTING.md)** - Complete setup and testing instructions
- **[LOGGING_GUIDE.md](guides/LOGGING_GUIDE.md)** - End-to-end request tracking and monitoring

### 🔧 Technical (`technical/`)
Technical specifications, schemas, and sample outputs.

- **[FRAUD_FLOW_TECHNICAL.md](technical/FRAUD_FLOW_TECHNICAL.md)** - Complete end-to-end transaction flow explanation
- **[fraud-flow-diagram.html](technical/fraud-flow-diagram.html)** - Interactive visual flow diagram
- **[FRAUD_NOTIFICATION_SERVICE.md](technical/FRAUD_NOTIFICATION_SERVICE.md)** - Alert system for dashboard and investigation
- **[DATABASE_SCHEMA.md](technical/DATABASE_SCHEMA.md)** - Complete database design and queries
- **[DECISION_SERVICE_SAMPLE_OUTPUT.md](technical/DECISION_SERVICE_SAMPLE_OUTPUT.md)** - Fraud decision examples
- **[EXPLAINABILITY_SAMPLE_OUTPUT.md](technical/EXPLAINABILITY_SAMPLE_OUTPUT.md)** - AI explanation examples

### 💻 Development (`development/`)
Development history, planning, and code review results.

- **[IMPLEMENTATION_PLAN.md](development/IMPLEMENTATION_PLAN.md)** - 16-week detailed implementation plan
- **[HACKATHON_PLAN.md](development/HACKATHON_PLAN.md)** - 5-day sprint plan
- **[CODE_REVIEW_SUMMARY.md](development/CODE_REVIEW_SUMMARY.md)** - Quality assurance results
- **[PROBLEMS_TAB_EXPLANATION.md](development/PROBLEMS_TAB_EXPLANATION.md)** - Common issues and solutions
- **[PROMPTS.md](development/PROMPTS.md)** - All Bob prompts used to build this platform
- **[FRAUD_ALERTS_PAGINATION_FIX.md](development/FRAUD_ALERTS_PAGINATION_FIX.md)** - Pagination and UI improvements (2026-05-11)

## 🚀 Quick Start

New to the project? Start here:

1. **[SETUP_TESTING.md](guides/SETUP_TESTING.md)** - Get the application running
2. **[FRAUD_FLOW_TECHNICAL.md](technical/FRAUD_FLOW_TECHNICAL.md)** - Understand the transaction flow
3. **[HACKATHON_ARCHITECTURE.md](architecture/HACKATHON_ARCHITECTURE.md)** - Understand the system design
4. **[DATABASE_SCHEMA.md](technical/DATABASE_SCHEMA.md)** - Learn the data model

## 🔍 Finding Documentation

### By Task
- **Setting up the project** → [SETUP_TESTING.md](guides/SETUP_TESTING.md)
- **Understanding transaction flow** → [FRAUD_FLOW_TECHNICAL.md](technical/FRAUD_FLOW_TECHNICAL.md)
- **Working with fraud alerts** → [FRAUD_NOTIFICATION_SERVICE.md](technical/FRAUD_NOTIFICATION_SERVICE.md)
- **Understanding architecture** → [HACKATHON_ARCHITECTURE.md](architecture/HACKATHON_ARCHITECTURE.md)
- **Viewing logs** → [LOGGING_GUIDE.md](guides/LOGGING_GUIDE.md)
- **Database queries** → [DATABASE_SCHEMA.md](technical/DATABASE_SCHEMA.md)
- **Understanding fraud decisions** → [DECISION_SERVICE_SAMPLE_OUTPUT.md](technical/DECISION_SERVICE_SAMPLE_OUTPUT.md)
- **Fixing pagination issues** → [FRAUD_ALERTS_PAGINATION_FIX.md](development/FRAUD_ALERTS_PAGINATION_FIX.md)

### By Role
- **Developers** → Start with `guides/` and `technical/`
- **Architects** → Focus on `architecture/`
- **Project Managers** → Review `development/` for planning
- **QA Engineers** → Check `guides/SETUP_TESTING.md` and `development/CODE_REVIEW_SUMMARY.md`

## 📝 Documentation Standards

All documentation in this project follows these standards:

- **Markdown format** for easy reading and version control
- **Clear headings** with emoji for visual navigation
- **Code examples** with syntax highlighting
- **Cross-references** between related documents
- **Table of contents** for longer documents

## 🔗 External References

- **Main README** → [../README.md](../README.md)
- **AGENTS.md** → [../AGENTS.md](../AGENTS.md) (AI assistant guidance)
- **Source Code** → [../src/](../src/)

## Made with Bob

This documentation structure was organized using Bob AI assistant for improved readability and maintainability.