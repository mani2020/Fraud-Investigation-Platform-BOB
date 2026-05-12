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
  Button,
  Tag,
} from '@carbon/react';
import { View, Renew } from '@carbon/icons-react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_ENDPOINTS } from '../../config/api';
import CustomPagination from '../common/CustomPagination';
import './TransactionMonitor.scss';

const headers = [
  { key: 'txnId', header: 'Transaction ID' },
  { key: 'timestamp', header: 'Timestamp' },
  { key: 'amount', header: 'Amount' },
  { key: 'customerId', header: 'Customer ID' },
  { key: 'merchant', header: 'Merchant' },
  { key: 'fraudScore', header: 'Fraud Score' },
  { key: 'status', header: 'Status' },
  { key: 'actions', header: 'Actions' },
];

const TransactionMonitor = () => {
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchTransactions();
    const interval = setInterval(fetchTransactions, 5000); // Refresh every 5 seconds
    return () => clearInterval(interval);
  }, []);

  const fetchTransactions = async () => {
    try {
      const response = await axios.get(API_ENDPOINTS.TRANSACTIONS);
      setTransactions(response.data);
      setFilteredTransactions(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching transactions:', error);
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    const value = e.target.value.toLowerCase();
    setSearchTerm(value);
    setPage(1); // Reset to first page on search

    if (!value.trim()) {
      setFilteredTransactions(transactions);
      return;
    }

    const filtered = transactions.filter(txn =>
      txn.txnId?.toLowerCase().includes(value) ||
      txn.customerId?.toLowerCase().includes(value) ||
      txn.merchant?.toLowerCase().includes(value) ||
      txn.status?.toLowerCase().includes(value)
    );
    
    setFilteredTransactions(filtered);
  };

  const getStatusTag = (status) => {
    const statusMap = {
      APPROVED: 'green',
      FLAGGED: 'red',
      PENDING: 'blue',
      REVIEW: 'orange',
    };
    return <Tag type={statusMap[status] || 'gray'}>{status}</Tag>;
  };

  const getFraudScoreTag = (score) => {
    // Backend sends scores as 0-100, not 0-1, so no multiplication needed
    const numScore = typeof score === 'number' ? score : parseFloat(score);
    if (numScore >= 80) return <Tag type="red">{numScore.toFixed(0)}%</Tag>;
    if (numScore >= 50) return <Tag type="orange">{numScore.toFixed(0)}%</Tag>;
    if (numScore >= 30) return <Tag type="yellow">{numScore.toFixed(0)}%</Tag>;
    return <Tag type="green">{numScore.toFixed(0)}%</Tag>;
  };

  const rows = filteredTransactions.map((txn) => ({
    id: txn.txnId,
    txnId: txn.txnId,
    timestamp: new Date(txn.timestamp).toLocaleString(),
    amount: `$${parseFloat(txn.amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
    customerId: txn.customerId,
    merchant: txn.merchant,
    fraudScore: txn.fraudScore ? getFraudScoreTag(parseFloat(txn.fraudScore)) : <Tag type="gray">N/A</Tag>,
    status: getStatusTag(txn.status),
    actions: (
      <Button
        kind="ghost"
        size="sm"
        renderIcon={View}
        iconDescription="View Details"
        onClick={() => navigate(`/investigation/${txn.txnId}`)}
      >
        View
      </Button>
    ),
  }));

  const paginatedRows = rows.slice((page - 1) * pageSize, page * pageSize);

  if (loading) {
    return (
      <div className="transaction-monitor-container">
        <div className="loading-state">
          <h1>Transaction Monitor</h1>
          <p>Loading transactions...</p>
        </div>
      </div>
    );
  }

  if (transactions.length === 0 && !loading) {
    return (
      <div className="transaction-monitor-container">
        <div className="monitor-header">
          <h1>Transaction Monitor</h1>
          <p className="subtitle">Monitor all transactions with fraud detection scores</p>
        </div>
        <div className="empty-state">
          <h3>No Transactions Found</h3>
          <p>No transaction data available at the moment.</p>
          <Button
            renderIcon={Renew}
            onClick={fetchTransactions}
          >
            Refresh
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="transaction-monitor-container">
      <div className="monitor-header">
        <h1>Transaction Monitor</h1>
        <p className="subtitle">Monitor all transactions with fraud detection scores</p>
      </div>

      <div className="toolbar-container">
        <div className="search-container">
          <TableToolbarSearch
            placeholder="Search by ID, Customer, Merchant, Status..."
            value={searchTerm}
            onChange={handleSearch}
            onClear={() => {
              setSearchTerm('');
              setFilteredTransactions(transactions);
            }}
          />
        </div>
        <Button
          className="refresh-button"
          renderIcon={Renew}
          onClick={fetchTransactions}
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
                    {headers.map((header) => {
                      const { key, ...headerProps } = getHeaderProps({ header });
                      return (
                        <TableHeader key={header.key} {...headerProps}>
                          {header.header}
                        </TableHeader>
                      );
                    })}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {rows.map((row) => {
                    const { key, ...rowProps } = getRowProps({ row });
                    return (
                      <TableRow key={row.id} {...rowProps}>
                        {row.cells.map((cell) => (
                          <TableCell key={cell.id}>{cell.value}</TableCell>
                        ))}
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DataTable>
      </div>
      
      <CustomPagination
        page={page}
        pageSize={pageSize}
        totalItems={filteredTransactions.length}
        pageSizes={[10, 20, 30, 40, 50]}
        onChange={({ page: newPage, pageSize: newPageSize }) => {
          setPage(newPage);
          setPageSize(newPageSize);
        }}
      />
    </div>
  );
};

export default TransactionMonitor;

// Made with Bob