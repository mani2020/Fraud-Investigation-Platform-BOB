# Fraud Flow Diagram Generation

This directory contains the fraud detection flow diagram in multiple formats.

## Files

- **`fraud-flow-diagram.mmd`** - Mermaid source file
- **`fraud-flow-diagram.html`** - Interactive HTML version (can be opened in browser)
- **`fraud-flow-diagram.png`** - PNG image (to be generated)

## How to Generate PNG Diagram

### Option 1: Using the HTML File (Easiest)
1. Open `fraud-flow-diagram.html` in any web browser
2. Wait for the diagram to render
3. Right-click on the diagram
4. Select "Save image as..."
5. Save as `fraud-flow-diagram.png` in this directory

### Option 2: Using Mermaid Live Editor
1. Go to https://mermaid.live
2. Copy the content from `fraud-flow-diagram.mmd`
3. Paste into the editor
4. Click "Download PNG" or "Download SVG"
5. Save as `fraud-flow-diagram.png` in this directory

### Option 3: Using Mermaid CLI (If Node.js is available)
```bash
# Install mermaid-cli globally
npm install -g @mermaid-js/mermaid-cli

# Generate PNG
mmdc -i docs/technical/fraud-flow-diagram.mmd -o docs/technical/fraud-flow-diagram.png -t default -b transparent
```

### Option 4: Using VS Code Extension
1. Install "Markdown Preview Mermaid Support" extension
2. Open `fraud-flow-diagram.mmd`
3. Use the preview pane to view the diagram
4. Right-click and save the image

## Diagram Overview

The diagram illustrates the complete end-to-end flow of a high-risk transaction through the fraud detection system:

1. **Synchronous Flow** (Blue section) - API request and initial storage
2. **Asynchronous Flow** (Orange section) - Kafka-based fraud analysis
3. **Query Flow** (Green section) - Retrieving final results

### Key Components Shown

- **TransactionController** - REST API entry point
- **TransactionService** - Business logic layer
- **PostgreSQL** - Transaction storage
- **Kafka** - Event streaming (`fraud-transactions` topic)
- **FraudOrchestratorService** - Coordinates fraud analysis
- **5 Fraud Agents** - Parallel execution (Risk, Geo, Device, AML, Behavior)
- **DecisionService** - Final decision logic
- **ExplainabilityService** - Human-readable explanations

## Related Documentation

- [Fraud Flow Technical Documentation](FRAUD_FLOW_TECHNICAL.md) - Detailed technical explanation
- [Architecture Overview](../architecture/ARCHITECTURE.md)
- [Database Schema](DATABASE_SCHEMA.md)