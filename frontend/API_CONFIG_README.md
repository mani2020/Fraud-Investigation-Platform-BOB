# API Configuration Guide

## Overview
This document explains the centralized API configuration for the Fraud Investigation Platform frontend.

## Configuration File
All API endpoints are configured in `src/config/api.js`.

## Base URL Configuration

### Development
- **Frontend**: `http://localhost:5174` (Vite dev server)
- **Backend API**: `http://localhost:8080` (Spring Boot)

The configuration automatically uses `http://localhost:8080` in development mode.

### Production
Set the `VITE_API_BASE_URL` environment variable:

```bash
VITE_API_BASE_URL=https://your-production-api.com
```

## Available Endpoints

### Transactions
- `GET /api/transactions` - Fetch all transactions
- `GET /api/transactions/{txnId}` - Fetch transaction by ID

### Fraud Alerts
- `GET /api/fraud-alerts` - Fetch all fraud alerts
- `PUT /api/fraud-alerts/{alertId}/resolve` - Resolve an alert
- `PUT /api/fraud-alerts/{alertId}/dismiss` - Dismiss an alert

### Analytics
- `GET /api/analytics` - Fetch analytics data
- `GET /api/analytics/fraud-by-country` - Fetch fraud heatmap data

## Usage in Components

### Import the configuration
```javascript
import { API_ENDPOINTS } from '../../config/api';
```

### Use predefined endpoints
```javascript
// Fetch transactions
const response = await axios.get(API_ENDPOINTS.TRANSACTIONS);

// Fetch transaction by ID
const response = await axios.get(API_ENDPOINTS.TRANSACTION_BY_ID(txnId));

// Resolve fraud alert
await axios.put(API_ENDPOINTS.FRAUD_ALERT_RESOLVE(alertId));
```

## Environment Variables

Create a `.env` file in the frontend directory (copy from `.env.example`):

```bash
# Development
VITE_API_BASE_URL=http://localhost:8080

# Production
VITE_API_BASE_URL=https://api.production.com
```

**Note**: `.env` files are gitignored. Use `.env.example` as a template.

## Updated Components

The following components have been updated to use the centralized API configuration:

1. `src/components/Alerts/FraudAlerts.jsx`
2. `src/components/Transactions/TransactionMonitor.jsx`
3. `src/components/Analytics/Analytics.jsx`
4. `src/components/Investigation/Investigation.jsx`
5. `src/pages/Dashboard.jsx`

## Benefits

✅ **Single source of truth** - All API URLs in one place  
✅ **Environment-aware** - Automatically switches between dev/prod  
✅ **Easy maintenance** - Update once, applies everywhere  
✅ **Type safety** - Predefined endpoint functions prevent typos  
✅ **Flexibility** - Easy to add new endpoints or modify existing ones

## Troubleshooting

### API calls failing with 404
- Verify backend is running on port 8080
- Check `API_BASE_URL` in browser console: `console.log(API_BASE_URL)`
- Ensure CORS is configured in Spring Boot backend

### Wrong base URL in production
- Check `VITE_API_BASE_URL` environment variable
- Rebuild the application after changing environment variables
- Verify the build includes the correct environment variables

## Made with Bob