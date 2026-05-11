# Frontend Implementation Guide

Complete guide to implementing the Fraud Investigation Dashboard with IBM Carbon Design System.

## Table of Contents

1. [Project Setup](#project-setup)
2. [API Services](#api-services)
3. [Dashboard Layout](#dashboard-layout)
4. [Transaction Monitor](#transaction-monitor)
5. [Fraud Alerts](#fraud-alerts)
6. [Investigation Details](#investigation-details)
7. [Risk Visualization](#risk-visualization)
8. [Analytics Dashboard](#analytics-dashboard)
9. [Custom Hooks](#custom-hooks)
10. [Utilities](#utilities)

---

## 1. Project Setup

### Install Dependencies

```bash
cd frontend
npm install
```

### Start Development Server

```bash
npm run dev
```

Access at: http://localhost:5174

---

## 2. API Services

### `src/services/api.js`

```javascript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export default api;
```

### `src/services/transactionService.js`

```javascript
import api from './api';

export const transactionService = {
  // Get all transactions
  getAllTransactions: async () => {
    const response = await api.get('/transactions');
    return response.data;
  },

  // Get transaction by ID
  getTransactionById: async (txnId) => {
    const response = await api.get(`/transactions/${txnId}`);
    return response.data;
  },

  // Get customer transactions
  getCustomerTransactions: async (customerId) => {
    const response = await api.get(`/transactions/customer/${customerId}`);
    return response.data;
  },

  // Submit new transaction
  submitTransaction: async (transactionData) => {
    const response = await api.post('/transactions', transactionData);
    return response.data;
  },
};
```

### `src/services/alertService.js`

```javascript
import api from './api';

export const alertService = {
  // Get all alerts
  getAllAlerts: async () => {
    const response = await api.get('/alerts');
    return response.data;
  },

  // Get critical alerts
  getCriticalAlerts: async () => {
    const response = await api.get('/alerts/critical');
    return response.data;
  },

  // Get unacknowledged alerts
  getUnacknowledgedAlerts: async () => {
    const response = await api.get('/alerts/unacknowledged');
    return response.data;
  },

  // Get alerts by severity
  getAlertsBySeverity: async (severity) => {
    const response = await api.get(`/alerts/severity/${severity}`);
    return response.data;
  },

  // Acknowledge alert
  acknowledgeAlert: async (alertId, acknowledgedBy) => {
    const response = await api.put(`/alerts/${alertId}/acknowledge`, {
      acknowledgedBy,
    });
    return response.data;
  },

  // Get alert statistics
  getAlertStats: async () => {
    const response = await api.get('/alerts/stats');
    return response.data;
  },
};
```

---

## 3. Dashboard Layout

### `src/components/Dashboard/DashboardLayout.jsx`

```javascript
import React, { useState } from 'react';
import {
  Header,
  HeaderName,
  HeaderNavigation,
  HeaderMenuItem,
  HeaderGlobalBar,
  HeaderGlobalAction,
  SideNav,
  SideNavItems,
  SideNavLink,
  SkipToContent,
} from '@carbon/react';
import {
  Dashboard,
  Notification,
  Search,
  ChartLine,
  UserAvatar,
} from '@carbon/icons-react';
import { useNavigate, useLocation } from 'react-router-dom';

const DashboardLayout = ({ children }) => {
  const [isSideNavExpanded, setIsSideNavExpanded] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <>
      <Header aria-label="Fraud Investigation Platform">
        <SkipToContent />
        <HeaderName href="/" prefix="IBM">
          Fraud Investigation
        </HeaderName>
        <HeaderNavigation aria-label="Navigation">
          <HeaderMenuItem onClick={() => navigate('/dashboard')}>
            Dashboard
          </HeaderMenuItem>
          <HeaderMenuItem onClick={() => navigate('/alerts')}>
            Alerts
          </HeaderMenuItem>
          <HeaderMenuItem onClick={() => navigate('/analytics')}>
            Analytics
          </HeaderMenuItem>
        </HeaderNavigation>
        <HeaderGlobalBar>
          <HeaderGlobalAction aria-label="Search">
            <Search size={20} />
          </HeaderGlobalAction>
          <HeaderGlobalAction aria-label="Notifications">
            <Notification size={20} />
          </HeaderGlobalAction>
          <HeaderGlobalAction aria-label="User Avatar">
            <UserAvatar size={20} />
          </HeaderGlobalAction>
        </HeaderGlobalBar>
      </Header>

      <SideNav
        aria-label="Side navigation"
        expanded={isSideNavExpanded}
        onOverlayClick={() => setIsSideNavExpanded(false)}
      >
        <SideNavItems>
          <SideNavLink
            renderIcon={Dashboard}
            onClick={() => navigate('/dashboard')}
            isActive={location.pathname === '/dashboard'}
          >
            Transaction Monitor
          </SideNavLink>
          <SideNavLink
            renderIcon={Notification}
            onClick={() => navigate('/alerts')}
            isActive={location.pathname === '/alerts'}
          >
            Fraud Alerts
          </SideNavLink>
          <SideNavLink
            renderIcon={ChartLine}
            onClick={() => navigate('/analytics')}
            isActive={location.pathname === '/analytics'}
          >
            Analytics
          </SideNavLink>
        </SideNavItems>
      </SideNav>

      <div style={{ marginTop: '3rem', marginLeft: isSideNavExpanded ? '16rem' : '3rem' }}>
        {children}
      </div>
    </>
  );
};

export default DashboardLayout;
```

---

## 4. Transaction Monitor

### `src/components/Transactions/TransactionMonitor.jsx`

```javascript
import React, { useState, useEffect } from 'react';
import {
  DataTable,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableHeader,
  TableBody,
  TableCell,
  Tag,
  Button,
  Loading,
} from '@carbon/react';
import { View } from '@carbon/icons-react';
import { useNavigate } from 'react-router-dom';
import { transactionService } from '../../services/transactionService';
import { formatCurrency, formatDate, getFraudScoreColor } from '../../utils/formatters';

const headers = [
  { key: 'txnId', header: 'Transaction ID' },
  { key: 'customerId', header: 'Customer ID' },
  { key: 'amount', header: 'Amount' },
  { key: 'merchant', header: 'Merchant' },
  { key: 'country', header: 'Country' },
  { key: 'fraudScore', header: 'Fraud Score' },
  { key: 'status', header: 'Status' },
  { key: 'decision', header: 'Decision' },
  { key: 'timestamp', header: 'Time' },
  { key: 'actions', header: 'Actions' },
];

const TransactionMonitor = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchTransactions();
    
    // Poll every 5 seconds
    const interval = setInterval(fetchTransactions, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchTransactions = async () => {
    try {
      const data = await transactionService.getAllTransactions();
      setTransactions(data);
    } catch (error) {
      console.error('Error fetching transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: 'blue',
      PROCESSED: 'green',
      FAILED: 'red',
    };
    return <Tag type={statusMap[status] || 'gray'}>{status}</Tag>;
  };

  const getDecisionTag = (decision) => {
    const decisionMap = {
      APPROVE: 'green',
      REVIEW: 'yellow',
      REJECT: 'red',
    };
    return decision ? <Tag type={decisionMap[decision] || 'gray'}>{decision}</Tag> : '-';
  };

  const rows = transactions.map((txn) => ({
    id: txn.id,
    txnId: txn.txnId,
    customerId: txn.customerId,
    amount: formatCurrency(txn.amount),
    merchant: txn.merchant,
    country: txn.country,
    fraudScore: txn.fraudScore ? (
      <span className={getFraudScoreColor(txn.fraudScore)}>
        {txn.fraudScore.toFixed(1)}
      </span>
    ) : '-',
    status: getStatusTag(txn.status),
    decision: getDecisionTag(txn.fraudDecision),
    timestamp: formatDate(txn.timestamp),
    actions: (
      <Button
        kind="ghost"
        size="sm"
        renderIcon={View}
        onClick={() => navigate(`/investigation/${txn.txnId}`)}
      >
        View
      </Button>
    ),
  }));

  if (loading) {
    return <Loading description="Loading transactions..." />;
  }

  return (
    <div className="p-2">
      <h2>Live Transaction Monitor</h2>
      <DataTable rows={rows} headers={headers}>
        {({ rows, headers, getTableProps, getHeaderProps, getRowProps }) => (
          <TableContainer title="Recent Transactions">
            <Table {...getTableProps()}>
              <TableHead>
                <TableRow>
                  {headers.map((header) => (
                    <TableHeader {...getHeaderProps({ header })}>
                      {header.header}
                    </TableHeader>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.map((row) => (
                  <TableRow {...getRowProps({ row })}>
                    {row.cells.map((cell) => (
                      <TableCell key={cell.id}>{cell.value}</TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </DataTable>
    </div>
  );
};

export default TransactionMonitor;
```

---

## 5. Fraud Alerts

### `src/components/Alerts/FraudAlerts.jsx`

```javascript
import React, { useState, useEffect } from 'react';
import {
  Tile,
  Tag,
  Button,
  Grid,
  Column,
  Tabs,
  TabList,
  Tab,
  TabPanels,
  TabPanel,
  Modal,
  TextInput,
} from '@carbon/react';
import { Warning, CheckmarkFilled } from '@carbon/icons-react';
import { alertService } from '../../services/alertService';
import { formatDate } from '../../utils/formatters';

const FraudAlerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [selectedTab, setSelectedTab] = useState(0);
  const [acknowledgeModal, setAcknowledgeModal] = useState(false);
  const [selectedAlert, setSelectedAlert] = useState(null);
  const [analyst, setAnalyst] = useState('');

  useEffect(() => {
    fetchAlerts();
    
    // Poll every 5 seconds
    const interval = setInterval(fetchAlerts, 5000);
    return () => clearInterval(interval);
  }, [selectedTab]);

  const fetchAlerts = async () => {
    try {
      let data;
      switch (selectedTab) {
        case 0: // All
          data = await alertService.getAllAlerts();
          break;
        case 1: // Critical
          data = await alertService.getCriticalAlerts();
          break;
        case 2: // Unacknowledged
          data = await alertService.getUnacknowledgedAlerts();
          break;
        default:
          data = await alertService.getAllAlerts();
      }
      setAlerts(data);
    } catch (error) {
      console.error('Error fetching alerts:', error);
    }
  };

  const handleAcknowledge = async () => {
    try {
      await alertService.acknowledgeAlert(selectedAlert.alertId, analyst);
      setAcknowledgeModal(false);
      setSelectedAlert(null);
      setAnalyst('');
      fetchAlerts();
    } catch (error) {
      console.error('Error acknowledging alert:', error);
    }
  };

  const getSeverityColor = (severity) => {
    const colors = {
      CRITICAL: 'red',
      HIGH: 'orange',
      MEDIUM: 'yellow',
      LOW: 'blue',
    };
    return colors[severity] || 'gray';
  };

  return (
    <div className="p-2">
      <h2>Fraud Alerts</h2>
      
      <Tabs selectedIndex={selectedTab} onChange={({ selectedIndex }) => setSelectedTab(selectedIndex)}>
        <TabList aria-label="Alert filters">
          <Tab>All Alerts</Tab>
          <Tab>Critical</Tab>
          <Tab>Unacknowledged</Tab>
        </TabList>
        <TabPanels>
          <TabPanel>
            <AlertGrid 
              alerts={alerts} 
              getSeverityColor={getSeverityColor}
              onAcknowledge={(alert) => {
                setSelectedAlert(alert);
                setAcknowledgeModal(true);
              }}
            />
          </TabPanel>
          <TabPanel>
            <AlertGrid 
              alerts={alerts} 
              getSeverityColor={getSeverityColor}
              onAcknowledge={(alert) => {
                setSelectedAlert(alert);
                setAcknowledgeModal(true);
              }}
            />
          </TabPanel>
          <TabPanel>
            <AlertGrid 
              alerts={alerts} 
              getSeverityColor={getSeverityColor}
              onAcknowledge={(alert) => {
                setSelectedAlert(alert);
                setAcknowledgeModal(true);
              }}
            />
          </TabPanel>
        </TabPanels>
      </Tabs>

      <Modal
        open={acknowledgeModal}
        onRequestClose={() => setAcknowledgeModal(false)}
        onRequestSubmit={handleAcknowledge}
        modalHeading="Acknowledge Alert"
        primaryButtonText="Acknowledge"
        secondaryButtonText="Cancel"
      >
        <TextInput
          id="analyst"
          labelText="Your Name/Email"
          value={analyst}
          onChange={(e) => setAnalyst(e.target.value)}
          placeholder="analyst@example.com"
        />
      </Modal>
    </div>
  );
};

const AlertGrid = ({ alerts, getSeverityColor, onAcknowledge }) => (
  <Grid className="mt-2">
    {alerts.map((alert) => (
      <Column key={alert.alertId} lg={4} md={4} sm={4}>
        <Tile className="mb-1">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Tag type={getSeverityColor(alert.severity)}>{alert.severity}</Tag>
            {alert.acknowledged ? (
              <CheckmarkFilled size={20} style={{ color: 'green' }} />
            ) : (
              <Warning size={20} style={{ color: 'red' }} />
            )}
          </div>
          <h4 className="mt-1">{alert.message}</h4>
          <p><strong>Transaction:</strong> {alert.txnId}</p>
          <p><strong>Score:</strong> {alert.fraudScore?.toFixed(1)}</p>
          <p><strong>Time:</strong> {formatDate(alert.timestamp)}</p>
          {!alert.acknowledged && (
            <Button size="sm" className="mt-1" onClick={() => onAcknowledge(alert)}>
              Acknowledge
            </Button>
          )}
        </Tile>
      </Column>
    ))}
  </Grid>
);

export default FraudAlerts;
```

---

## 6. Investigation Details

### `src/components/Investigation/Investigation.jsx`

```javascript
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import {
  Tile,
  Accordion,
  AccordionItem,
  Tag,
  Loading,
  ProgressBar,
} from '@carbon/react';
import { transactionService } from '../../services/transactionService';
import { formatCurrency, formatDate } from '../../utils/formatters';

const Investigation = () => {
  const { txnId } = useParams();
  const [transaction, setTransaction] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTransaction();
  }, [txnId]);

  const fetchTransaction = async () => {
    try {
      const data = await transactionService.getTransactionById(txnId);
      setTransaction(data);
    } catch (error) {
      console.error('Error fetching transaction:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <Loading description="Loading investigation details..." />;
  }

  if (!transaction) {
    return <div>Transaction not found</div>;
  }

  return (
    <div className="p-2">
      <h2>Investigation: {transaction.txnId}</h2>
      
      <Tile className="mt-2">
        <h3>Transaction Details</h3>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div>
            <p><strong>Customer ID:</strong> {transaction.customerId}</p>
            <p><strong>Amount:</strong> {formatCurrency(transaction.amount)}</p>
            <p><strong>Merchant:</strong> {transaction.merchant}</p>
            <p><strong>Country:</strong> {transaction.country}</p>
          </div>
          <div>
            <p><strong>Device ID:</strong> {transaction.deviceId}</p>
            <p><strong>Payment Type:</strong> {transaction.paymentType}</p>
            <p><strong>Time:</strong> {formatDate(transaction.timestamp)}</p>
            <p><strong>Status:</strong> <Tag>{transaction.status}</Tag></p>
          </div>
        </div>
      </Tile>

      {transaction.fraudScore && (
        <Tile className="mt-2">
          <h3>Fraud Analysis</h3>
          <div className="mt-1">
            <p><strong>Fraud Score:</strong> {transaction.fraudScore.toFixed(1)}/100</p>
            <ProgressBar 
              value={transaction.fraudScore} 
              max={100}
              label="Risk Level"
            />
          </div>
          <div className="mt-2">
            <p><strong>Decision:</strong> <Tag type={
              transaction.fraudDecision === 'APPROVE' ? 'green' :
              transaction.fraudDecision === 'REVIEW' ? 'yellow' : 'red'
            }>{transaction.fraudDecision}</Tag></p>
          </div>
        </Tile>
      )}

      <Tile className="mt-2">
        <h3>Agent Analysis</h3>
        <Accordion>
          <AccordionItem title="Risk Agent">
            <p>Analyzes transaction amount, velocity, and patterns</p>
            <p><strong>Score:</strong> 75.0</p>
            <p><strong>Decision:</strong> REVIEW</p>
          </AccordionItem>
          <AccordionItem title="Geo Agent">
            <p>Checks country risk and location anomalies</p>
            <p><strong>Score:</strong> 85.0</p>
            <p><strong>Decision:</strong> REJECT</p>
          </AccordionItem>
          <AccordionItem title="Device Agent">
            <p>Validates device fingerprint and trust status</p>
            <p><strong>Score:</strong> 60.0</p>
            <p><strong>Decision:</strong> REVIEW</p>
          </AccordionItem>
          <AccordionItem title="AML Agent">
            <p>Anti-Money Laundering checks and watchlist screening</p>
            <p><strong>Score:</strong> 45.0</p>
            <p><strong>Decision:</strong> APPROVE</p>
          </AccordionItem>
          <AccordionItem title="Behavior Agent">
            <p>Customer behavior and spending pattern analysis</p>
            <p><strong>Score:</strong> 70.0</p>
            <p><strong>Decision:</strong> REVIEW</p>
          </AccordionItem>
        </Accordion>
      </Tile>
    </div>
  );
};

export default Investigation;
```

---

## 7. Risk Visualization

### `src/components/Analytics/RiskVisualization.jsx`

```javascript
import React from 'react';
import { GaugeChart, DonutChart } from '@carbon/charts-react';
import '@carbon/charts/styles.css';

const RiskVisualization = ({ fraudScore, agentScores }) => {
  const gaugeData = [
    {
      group: 'value',
      value: fraudScore,
    },
  ];

  const gaugeOptions = {
    title: 'Fraud Risk Score',
    resizable: true,
    height: '250px',
    gauge: {
      type: 'semi',
      status: fraudScore >= 70 ? 'danger' : fraudScore >= 40 ? 'warning' : 'success',
    },
  };

  const donutData = agentScores.map((agent) => ({
    group: agent.name,
    value: agent.score,
  }));

  const donutOptions = {
    title: 'Agent Contribution',
    resizable: true,
    height: '250px',
    donut: {
      center: {
        label: 'Agents',
      },
    },
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
      <GaugeChart data={gaugeData} options={gaugeOptions} />
      <DonutChart data={donutData} options={donutOptions} />
    </div>
  );
};

export default RiskVisualization;
```

---

## 8. Analytics Dashboard

### `src/components/Analytics/Analytics.jsx`

```javascript
import React, { useState, useEffect } from 'react';
import { Tile, Grid, Column } from '@carbon/react';
import { LineChart, BarChart } from '@carbon/charts-react';
import { alertService } from '../../services/alertService';

const Analytics = () => {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const data = await alertService.getAlertStats();
      setStats(data);
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };

  const trendData = [
    { group: 'Fraud Rate', date: '2026-05-01', value: 2.5 },
    { group: 'Fraud Rate', date: '2026-05-02', value: 3.1 },
    { group: 'Fraud Rate', date: '2026-05-03', value: 2.8 },
    { group: 'Fraud Rate', date: '2026-05-04', value: 3.5 },
    { group: 'Fraud Rate', date: '2026-05-05', value: 4.2 },
  ];

  const trendOptions = {
    title: 'Fraud Rate Trend',
    axes: {
      bottom: {
        title: 'Date',
        mapsTo: 'date',
        scaleType: 'time',
      },
      left: {
        mapsTo: 'value',
        title: 'Fraud Rate (%)',
      },
    },
    height: '400px',
  };

  return (
    <div className="p-2">
      <h2>Fraud Analytics</h2>
      
      <Grid className="mt-2">
        <Column lg={3} md={4} sm={4}>
          <Tile>
            <h4>Critical Alerts</h4>
            <h2>{stats?.CRITICAL || 0}</h2>
          </Tile>
        </Column>
        <Column lg={3} md={4} sm={4}>
          <Tile>
            <h4>High Alerts</h4>
            <h2>{stats?.HIGH || 0}</h2>
          </Tile>
        </Column>
        <Column lg={3} md={4} sm={4}>
          <Tile>
            <h4>Medium Alerts</h4>
            <h2>{stats?.MEDIUM || 0}</h2>
          </Tile>
        </Column>
        <Column lg={3} md={4} sm={4}>
          <Tile>
            <h4>Low Alerts</h4>
            <h2>{stats?.LOW || 0}</h2>
          </Tile>
        </Column>
      </Grid>

      <Tile className="mt-2">
        <LineChart data={trendData} options={trendOptions} />
      </Tile>
    </div>
  );
};

export default Analytics;
```

---

## 9. Custom Hooks

### `src/hooks/usePolling.js`

```javascript
import { useEffect, useRef } from 'react';

export const usePolling = (callback, interval = 5000) => {
  const savedCallback = useRef();

  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  useEffect(() => {
    const tick = () => {
      savedCallback.current();
    };

    const id = setInterval(tick, interval);
    return () => clearInterval(id);
  }, [interval]);
};
```

---

## 10. Utilities

### `src/utils/formatters.js`

```javascript
export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
};

export const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const getFraudScoreColor = (score) => {
  if (score >= 70) return 'score-critical';
  if (score >= 50) return 'score-high';
  if (score >= 30) return 'score-medium';
  return 'score-low';
};
```

### `src/utils/constants.js`

```javascript
export const SEVERITY_LEVELS = {
  CRITICAL: 'CRITICAL',
  HIGH: 'HIGH',
  MEDIUM: 'MEDIUM',
  LOW: 'LOW',
};

export const DECISION_TYPES = {
  APPROVE: 'APPROVE',
  REVIEW: 'REVIEW',
  REJECT: 'REJECT',
};

export const POLLING_INTERVAL = 5000; // 5 seconds
```

---

## Next Steps

1. **Install dependencies**: `npm install`
2. **Start backend**: Ensure Spring Boot app is running on port 8080
3. **Start frontend**: `npm run dev`
4. **Test features**: Navigate through dashboard, alerts, and analytics
5. **Customize**: Adjust colors, layouts, and polling intervals as needed

## Additional Resources

- [IBM Carbon Design System](https://carbondesignsystem.com/)
- [Carbon React Components](https://react.carbondesignsystem.com/)
- [Carbon Charts](https://charts.carbondesignsystem.com/)
- [React Router](https://reactrouter.com/)

---

**Made with ❤️ using IBM Carbon Design System**