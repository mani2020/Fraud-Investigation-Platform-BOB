import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Security,
  CheckmarkFilled,
  WarningAlt,
  ErrorFilled,
} from '@carbon/icons-react';
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
} from '@carbon/react';
import PageHeader from '../components/layout/PageHeader';
import DashboardGrid from '../components/dashboard/DashboardGrid';
import PremiumCard from '../components/common/PremiumCard';
import MetricCard from '../components/common/MetricCard';
import StatusBadge from '../components/common/StatusBadge';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';
import './Dashboard.scss';

const Dashboard = () => {
  const navigate = useNavigate();
  const [metrics, setMetrics] = useState({
    totalTransactions: 0,
    approvedTransactions: 0,
    flaggedTransactions: 0,
    blockedTransactions: 0,
    avgFraudScore: 0,
    activeAlerts: 0,
  });
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 10000); // Poll every 10 seconds
    return () => clearInterval(interval);
  }, []);

  const fetchDashboardData = async () => {
    try {
      // Fetch transactions
      const transactionsRes = await axios.get(API_ENDPOINTS.TRANSACTIONS);
      const transactions = transactionsRes.data;

      // Try to fetch alerts, but don't fail if endpoint doesn't exist
      let alerts = [];
      try {
        const alertsRes = await axios.get(API_ENDPOINTS.FRAUD_ALERTS);
        alerts = alertsRes.data;
      } catch (alertError) {
        // Fraud alerts endpoint not available yet
        console.log('Fraud alerts endpoint not available');
        alerts = [];
      }

      // Calculate metrics based on fraudDecision field
      const approved = transactions.filter(t => t.fraudDecision === 'APPROVE').length;
      const flagged = transactions.filter(t => t.fraudDecision === 'OTP' || t.fraudDecision === 'HOLD').length;
      const blocked = transactions.filter(t => t.fraudDecision === 'BLOCK').length;
      const avgScore = transactions.reduce((sum, t) => sum + (t.fraudScore || 0), 0) / transactions.length || 0;

      setMetrics({
        totalTransactions: transactions.length,
        approvedTransactions: approved,
        flaggedTransactions: flagged,
        blockedTransactions: blocked,
        avgFraudScore: avgScore, // Backend already sends 0-100, no multiplication needed
        activeAlerts: alerts.filter(a => a.status === 'OPEN').length,
      });

      setRecentTransactions(transactions.slice(0, 5));
      setLoading(false);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setLoading(false);
    }
  };

  const getFraudScoreColor = (score) => {
    if (score >= 70) return 'danger';
    if (score >= 50) return 'warning';
    if (score >= 30) return 'info';
    return 'success';
  };

  const getFraudScoreStatus = (score) => {
    if (score >= 70) return 'CRITICAL';
    if (score >= 50) return 'HIGH';
    if (score >= 30) return 'MEDIUM';
    return 'LOW';
  };

  return (
    <div className="premium-dashboard">
      {/* Page Header */}
      <PageHeader
        title="Fraud Investigation Dashboard"
        subtitle="Real-time monitoring and analysis of payment transactions"
        icon={Security}

      />

      {/* Hero Stats */}
      <div className="dashboard-hero-stats">
        <div className="stat-item">
          <span className="stat-value">{metrics.totalTransactions}</span>
          <span className="stat-label">Total Transactions</span>
        </div>
        <div className="stat-divider"></div>
        <div className="stat-item">
          <span className="stat-value">{metrics.activeAlerts}</span>
          <span className="stat-label">Active Alerts</span>
        </div>
        <div className="stat-divider"></div>
        <div className="stat-item">
          <span className="stat-value">{metrics.avgFraudScore.toFixed(1)}%</span>
          <span className="stat-label">Avg Fraud Score</span>
        </div>
      </div>

      {/* Metrics Grid */}
      <DashboardGrid columns={3} gap="lg">
        <MetricCard
          value={metrics.approvedTransactions}
          label="Approved Transactions"
          icon={CheckmarkFilled}
          variant="success"
          onClick={() => navigate('/transactions', { state: { filter: 'APPROVE' } })}
        />
        <MetricCard
          value={metrics.flaggedTransactions}
          label="Flagged for Review"
          icon={WarningAlt}
          variant="warning"
          onClick={() => navigate('/transactions', { state: { filter: 'FLAGGED' } })}
        />
        <MetricCard
          value={metrics.blockedTransactions}
          label="Blocked Transactions"
          icon={ErrorFilled}
          variant="danger"
          onClick={() => navigate('/transactions', { state: { filter: 'BLOCK' } })}
        />
      </DashboardGrid>

      {/* Recent Transactions - Full Width */}
      <DashboardGrid columns={1} gap="lg">
        <PremiumCard
          title="Recent Transactions"
          subtitle="Latest payment activity with fraud scores"
          variant="gradient"
          className="recent-transactions-table"
        >
          {recentTransactions.length > 0 ? (
            <DataTable
              rows={recentTransactions.slice(0, 10).map((txn, index) => ({
                id: txn.txnId || `txn-${index}`,
                txnId: txn.txnId || `TXN-${1000 + index}`,
                amount: txn.amount || 0,
                timestamp: txn.timestamp,
                fraudScore: txn.fraudScore || 0,
                decision: txn.fraudDecision || 'PENDING',
              }))}
              headers={[
                { key: 'txnId', header: 'Transaction ID' },
                { key: 'amount', header: 'Amount' },
                { key: 'timestamp', header: 'Time' },
                { key: 'fraudScore', header: 'Fraud Score' },
                { key: 'decision', header: 'Decision' },
              ]}
            >
              {({ rows, headers, getTableProps, getHeaderProps, getRowProps, getTableContainerProps }) => (
                <TableContainer {...getTableContainerProps()}>
                  <Table {...getTableProps()} size="md">
                    <TableHead>
                      <TableRow>
                        {headers.map((header) => (
                          <TableHeader key={header.key} {...getHeaderProps({ header })}>
                            {header.header}
                          </TableHeader>
                        ))}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rows.map((row) => (
                        <TableRow
                          key={row.id}
                          {...getRowProps({ row })}
                          onClick={() => navigate(`/investigation/${row.cells[0].value}`)}
                        >
                          {row.cells.map((cell) => {
                            if (cell.info.header === 'amount') {
                              return (
                                <TableCell key={cell.id}>
                                  ${parseFloat(cell.value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                </TableCell>
                              );
                            }
                            if (cell.info.header === 'timestamp') {
                              return (
                                <TableCell key={cell.id}>
                                  {new Date(cell.value).toLocaleString()}
                                </TableCell>
                              );
                            }
                            if (cell.info.header === 'fraudScore') {
                              const score = parseFloat(cell.value);
                              return (
                                <TableCell key={cell.id}>
                                  <Tag type={score >= 70 ? 'red' : score >= 50 ? 'purple' : 'green'}>
                                    {score.toFixed(1)}%
                                  </Tag>
                                </TableCell>
                              );
                            }
                            if (cell.info.header === 'decision') {
                              const decisionColors = {
                                'APPROVE': 'green',
                                'OTP': 'blue',
                                'HOLD': 'purple',
                                'BLOCK': 'red',
                                'PENDING': 'gray'
                              };
                              return (
                                <TableCell key={cell.id}>
                                  <Tag type={decisionColors[cell.value] || 'gray'}>
                                    {cell.value}
                                  </Tag>
                                </TableCell>
                              );
                            }
                            return <TableCell key={cell.id}>{cell.value}</TableCell>;
                          })}
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </DataTable>
          ) : (
            <div className="empty-state">
              <Security size={48} />
              <p>No recent transactions</p>
            </div>
          )}
        </PremiumCard>

      </DashboardGrid>
    </div>
  );
};

export default Dashboard;

// Made with Bob