/**
 * API Configuration
 * Centralized configuration for API base URL and endpoints
 */

// Determine the API base URL based on environment
const getApiBaseUrl = () => {
  // In production, API might be on same domain or different domain
  if (import.meta.env.PROD) {
    return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  }
  
  // In development, always use port 8080 for Spring Boot backend
  return 'http://localhost:8080';
};

export const API_BASE_URL = getApiBaseUrl();

// API Endpoints
export const API_ENDPOINTS = {
  // Transaction endpoints
  TRANSACTIONS: `${API_BASE_URL}/api/transactions`,
  TRANSACTION_BY_ID: (txnId) => `${API_BASE_URL}/api/transactions/${txnId}`,
  
  // Investigation endpoints
  INVESTIGATION_DETAILS: (txnId) => `${API_BASE_URL}/api/investigation/${txnId}`,
  
  // Fraud alert endpoints
  FRAUD_ALERTS: `${API_BASE_URL}/api/fraud-alerts`,
  FRAUD_ALERT_RESOLVE: (alertId) => `${API_BASE_URL}/api/fraud-alerts/${alertId}/resolve`,
  FRAUD_ALERT_DISMISS: (alertId) => `${API_BASE_URL}/api/fraud-alerts/${alertId}/dismiss`,
  
  // Analytics endpoints
  ANALYTICS: `${API_BASE_URL}/api/analytics`,
  ANALYTICS_FRAUD_BY_COUNTRY: `${API_BASE_URL}/api/analytics/fraud-by-country`,
};

// Export default axios configuration
export const apiConfig = {
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
};

export default {
  API_BASE_URL,
  API_ENDPOINTS,
  apiConfig,
};

// Made with Bob