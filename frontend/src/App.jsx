import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Theme } from '@carbon/react';
import AppLayout from './layouts/AppLayout';
import Dashboard from './pages/Dashboard';
import TransactionMonitor from './components/Transactions/TransactionMonitor';
import FraudAlerts from './components/Alerts/FraudAlerts';
import Investigation from './components/Investigation/Investigation';
import Analytics from './components/Analytics/Analytics';

/**
 * App - Main application component
 * 
 * Features:
 * - Carbon Design System theming
 * - React Router for navigation
 * - New responsive AppLayout
 * - Route configuration
 */
function App() {
  return (
    <Theme theme="g100">
      <Router>
        <AppLayout>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/transactions" element={<TransactionMonitor />} />
            <Route path="/alerts" element={<FraudAlerts />} />
            <Route path="/investigation/:txnId" element={<Investigation />} />
            <Route path="/investigation" element={<Investigation />} />
            <Route path="/analytics" element={<Analytics />} />
          </Routes>
        </AppLayout>
      </Router>
    </Theme>
  );
}

export default App;

// Made with Bob
