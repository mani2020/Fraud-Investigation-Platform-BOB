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
  TableToolbarSearch,
  Tag,
  Button,
} from '@carbon/react';
import { View, Renew } from '@carbon/icons-react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_ENDPOINTS } from '../../config/api';
import CustomPagination from '../common/CustomPagination';
import './FraudAlerts.scss';

const headers = [
  { key: 'txnId', header: 'Transaction ID' },
  { key: 'customerId', header: 'Customer ID' },
  { key: 'amount', header: 'Amount' },
  { key: 'timestamp', header: 'Timestamp' },
  { key: 'fraudScore', header: 'Fraud Score' },
  { key: 'severity', header: 'Risk Level' },
  { key: 'status', header: 'Status' },
  { key: 'actions', header: 'Actions' },
];

const FraudAlerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [filteredAlerts, setFilteredAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchAlerts();
    const interval = setInterval(fetchAlerts, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchAlerts = async () => {
    try {
      // Fetch fraud alerts from dedicated endpoint
      const response = await axios.get(API_ENDPOINTS.FRAUD_ALERTS);
      const fraudAlerts = response.data;
      
      // Sort by fraud score descending
      fraudAlerts.sort((a, b) =>
        parseFloat(b.fraudScore || 0) - parseFloat(a.fraudScore || 0)
      );
      
      setAlerts(fraudAlerts);
      setFilteredAlerts(fraudAlerts);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching alerts:', error);
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    const value = e.target.value.toLowerCase();
    setSearchTerm(value);
    setPage(1); // Reset to first page on search

    if (!value.trim()) {
      setFilteredAlerts(alerts);
      return;
    }

    const filtered = alerts.filter(alert =>
      alert.txnId?.toLowerCase().includes(value) ||
      alert.customerId?.toLowerCase().includes(value) ||
      alert.merchant?.toLowerCase().includes(value) ||
      alert.status?.toLowerCase().includes(value)
    );
    
    setFilteredAlerts(filtered);
  };

  const calculateSeverity = (fraudScore) => {
    // Backend sends scores as 0-100, not 0-1
    const score = parseFloat(fraudScore);
    if (score >= 80) return 'CRITICAL';
    if (score >= 70) return 'HIGH';
    if (score >= 50) return 'MEDIUM';
    return 'LOW';
  };

  const getSeverityTag = (fraudScore) => {
    const severity = calculateSeverity(fraudScore);
    const severityMap = {
      CRITICAL: 'red',
      HIGH: 'magenta',
      MEDIUM: 'purple',
      LOW: 'blue',
    };
    return <Tag type={severityMap[severity] || 'gray'}>{severity}</Tag>;
  };

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: 'blue',
      APPROVED: 'green',
      REJECTED: 'red',
      REVIEW: 'purple',
    };
    return <Tag type={statusMap[status] || 'gray'}>{status}</Tag>;
  };

  const handleResolve = async (alertId) => {
    try {
      await axios.put(API_ENDPOINTS.FRAUD_ALERT_RESOLVE(alertId));
      fetchAlerts();
    } catch (error) {
      console.error('Error resolving alert:', error);
    }
  };

  const handleDismiss = async (alertId) => {
    try {
      await axios.put(API_ENDPOINTS.FRAUD_ALERT_DISMISS(alertId));
      fetchAlerts();
    } catch (error) {
      console.error('Error dismissing alert:', error);
    }
  };

  const rows = filteredAlerts.map((alert) => ({
    id: alert.txnId,
    txnId: alert.txnId,
    customerId: alert.customerId,
    amount: `$${parseFloat(alert.amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
    timestamp: new Date(alert.timestamp).toLocaleString(),
    fraudScore: parseFloat(alert.fraudScore).toFixed(1) + '%',
    severity: getSeverityTag(alert.fraudScore),
    status: getStatusTag(alert.status),
    actions: (
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <Button
          kind="ghost"
          size="sm"
          renderIcon={View}
          iconDescription="View Details"
          onClick={() => navigate(`/investigation/${alert.txnId}`)}
        >
          View
        </Button>
      </div>
    ),
  }));

  const paginatedRows = rows.slice((page - 1) * pageSize, page * pageSize);

  if (loading) {
    return (
      <div className="fraud-alerts-container">
        <div className="loading-state">
          <h1>Fraud Alerts</h1>
          <p>Loading alerts...</p>
        </div>
      </div>
    );
  }

  if (alerts.length === 0 && !loading) {
    return (
      <div className="fraud-alerts-container">
        <div className="alerts-header">
          <h1>Active Fraud Alerts</h1>
          <p className="subtitle">Review and manage fraud detection alerts</p>
        </div>
        <div className="empty-state">
          <h3>✓ No Active Alerts</h3>
          <p>All clear! There are currently no fraud alerts requiring attention.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="fraud-alerts-container">
      <div className="alerts-header">
        <h1>Active Fraud Alerts</h1>
        <p className="subtitle">Review and manage fraud detection alerts</p>
      </div>

      <div className="toolbar-container">
        <div className="search-container">
          <TableToolbarSearch
            placeholder="Search by ID, Customer, Merchant, Status..."
            value={searchTerm}
            onChange={handleSearch}
            onClear={() => {
              setSearchTerm('');
              setFilteredAlerts(alerts);
            }}
          />
        </div>
        <Button
          className="refresh-button"
          renderIcon={Renew}
          onClick={fetchAlerts}
        >
          Refresh
        </Button>
      </div>
      
      <div className="table-container">
        <DataTable rows={paginatedRows} headers={headers}>
          {({
            rows,
            headers,
            getTableProps,
            getHeaderProps,
            getRowProps,
            getTableContainerProps,
          }) => (
            <TableContainer {...getTableContainerProps()}>
              <Table {...getTableProps()}>
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
                    <TableRow key={row.id} {...getRowProps({ row })}>
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
      
      <CustomPagination
        page={page}
        pageSize={pageSize}
        totalItems={filteredAlerts.length}
        pageSizes={[10, 20, 30, 40, 50]}
        onChange={({ page: newPage, pageSize: newPageSize }) => {
          setPage(newPage);
          setPageSize(newPageSize);
        }}
      />
    </div>
  );
};

export default FraudAlerts;

// Made with Bob