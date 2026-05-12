import React, { useState } from 'react';
import PropTypes from 'prop-types';
import {
  DataTable,
  Table,
  TableHead,
  TableRow,
  TableHeader,
  TableBody,
  TableCell,
  Pagination,
  Tag,
} from '@carbon/react';
import { ArrowUp, ArrowDown } from '@carbon/icons-react';
import StatusBadge from '../common/StatusBadge';
import './TransactionTable.scss';

/**
 * TransactionTable - Enterprise-grade transaction table
 * 
 * Features:
 * - Sticky header
 * - Responsive columns (hide less important on mobile)
 * - Pagination
 * - Sorting
 * - Fraud score badges
 * - Status pills
 * - Row hover effects
 * - Mobile card view (stack columns vertically)
 */
const TransactionTable = ({ transactions, onRowClick }) => {
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortColumn, setSortColumn] = useState('timestamp');
  const [sortDirection, setSortDirection] = useState('desc');

  // Define table headers
  const headers = [
    { key: 'transactionId', header: 'Transaction ID', sortable: true, priority: 'high' },
    { key: 'timestamp', header: 'Time', sortable: true, priority: 'high' },
    { key: 'amount', header: 'Amount', sortable: true, priority: 'high' },
    { key: 'fromAccount', header: 'From Account', sortable: false, priority: 'medium' },
    { key: 'toAccount', header: 'To Account', sortable: false, priority: 'medium' },
    { key: 'fraudScore', header: 'Fraud Score', sortable: true, priority: 'high' },
    { key: 'status', header: 'Status', sortable: true, priority: 'high' },
  ];

  // Sort transactions
  const sortedTransactions = [...transactions].sort((a, b) => {
    const aVal = a[sortColumn];
    const bVal = b[sortColumn];
    
    if (sortDirection === 'asc') {
      return aVal > bVal ? 1 : -1;
    }
    return aVal < bVal ? 1 : -1;
  });

  // Paginate transactions
  const startIndex = (page - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const paginatedTransactions = sortedTransactions.slice(startIndex, endIndex);

  // Handle sort
  const handleSort = (columnKey) => {
    if (sortColumn === columnKey) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortColumn(columnKey);
      setSortDirection('desc');
    }
  };

  // Get fraud score color
  const getFraudScoreColor = (score) => {
    // Backend sends scores as 0-100, not 0-1, so no multiplication needed
    const numScore = typeof score === 'number' ? score : parseFloat(score);
    if (numScore >= 70) return 'danger';
    if (numScore >= 50) return 'warning';
    if (numScore >= 30) return 'info';
    return 'success';
  };

  // Get fraud score status
  const getFraudScoreStatus = (score) => {
    // Backend sends scores as 0-100, not 0-1, so no multiplication needed
    const numScore = typeof score === 'number' ? score : parseFloat(score);
    if (numScore >= 70) return 'CRITICAL';
    if (numScore >= 50) return 'HIGH';
    if (numScore >= 30) return 'MEDIUM';
    return 'LOW';
  };

  // Format amount
  const formatAmount = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  // Format timestamp
  const formatTimestamp = (timestamp) => {
    return new Date(timestamp).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="transaction-table">
      {/* Desktop Table View */}
      <div className="transaction-table__desktop">
        <DataTable rows={paginatedTransactions} headers={headers}>
          {({ rows, headers, getTableProps, getHeaderProps, getRowProps }) => (
            <Table {...getTableProps()} className="transaction-table__table">
              <TableHead>
                <TableRow>
                  {headers.map((header) => (
                    <TableHeader
                      {...getHeaderProps({ header })}
                      key={header.key}
                      className={`transaction-table__header transaction-table__header--${header.priority}`}
                      onClick={() => header.sortable && handleSort(header.key)}
                      isSortable={header.sortable}
                    >
                      <div className="transaction-table__header-content">
                        {header.header}
                        {header.sortable && sortColumn === header.key && (
                          <span className="transaction-table__sort-icon">
                            {sortDirection === 'asc' ? (
                              <ArrowUp size={16} />
                            ) : (
                              <ArrowDown size={16} />
                            )}
                          </span>
                        )}
                      </div>
                    </TableHeader>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.map((row) => (
                  <TableRow
                    {...getRowProps({ row })}
                    key={row.id}
                    className="transaction-table__row"
                    onClick={() => onRowClick && onRowClick(row)}
                  >
                    {row.cells.map((cell) => {
                      const header = headers.find(h => h.key === cell.info.header);
                      return (
                        <TableCell
                          key={cell.id}
                          className={`transaction-table__cell transaction-table__cell--${header.priority}`}
                        >
                          {cell.info.header === 'fraudScore' ? (
                            <StatusBadge
                              status={getFraudScoreStatus(cell.value)}
                              type={getFraudScoreColor(cell.value)}
                              glow
                            />
                          ) : cell.info.header === 'status' ? (
                            <Tag
                              type={
                                cell.value === 'APPROVED'
                                  ? 'green'
                                  : cell.value === 'FLAGGED'
                                  ? 'orange'
                                  : 'red'
                              }
                            >
                              {cell.value}
                            </Tag>
                          ) : cell.info.header === 'amount' ? (
                            <span className="transaction-table__amount">
                              {formatAmount(cell.value)}
                            </span>
                          ) : cell.info.header === 'timestamp' ? (
                            <span className="transaction-table__timestamp">
                              {formatTimestamp(cell.value)}
                            </span>
                          ) : (
                            cell.value
                          )}
                        </TableCell>
                      );
                    })}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </DataTable>
      </div>

      {/* Mobile Card View */}
      <div className="transaction-table__mobile">
        {paginatedTransactions.map((txn, index) => (
          <div
            key={index}
            className="transaction-card"
            onClick={() => onRowClick && onRowClick(txn)}
          >
            <div className="transaction-card__header">
              <span className="transaction-card__id">{txn.transactionId}</span>
              <StatusBadge
                status={getFraudScoreStatus(txn.fraudScore)}
                type={getFraudScoreColor(txn.fraudScore)}
                glow
              />
            </div>
            <div className="transaction-card__amount">
              {formatAmount(txn.amount)}
            </div>
            <div className="transaction-card__details">
              <div className="transaction-card__detail">
                <span className="transaction-card__label">From:</span>
                <span className="transaction-card__value">{txn.fromAccount}</span>
              </div>
              <div className="transaction-card__detail">
                <span className="transaction-card__label">To:</span>
                <span className="transaction-card__value">{txn.toAccount}</span>
              </div>
              <div className="transaction-card__detail">
                <span className="transaction-card__label">Time:</span>
                <span className="transaction-card__value">
                  {formatTimestamp(txn.timestamp)}
                </span>
              </div>
            </div>
            <div className="transaction-card__footer">
              <Tag
                type={
                  txn.status === 'APPROVED'
                    ? 'green'
                    : txn.status === 'FLAGGED'
                    ? 'orange'
                    : 'red'
                }
              >
                {txn.status}
              </Tag>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="transaction-table__pagination">
        <Pagination
          page={page}
          pageSize={pageSize}
          pageSizes={[10, 20, 50]}
          totalItems={transactions.length}
          onChange={({ page, pageSize }) => {
            setPage(page);
            setPageSize(pageSize);
          }}
        />
      </div>
    </div>
  );
};

TransactionTable.propTypes = {
  transactions: PropTypes.arrayOf(
    PropTypes.shape({
      transactionId: PropTypes.string.isRequired,
      timestamp: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      amount: PropTypes.number.isRequired,
      fromAccount: PropTypes.string.isRequired,
      toAccount: PropTypes.string.isRequired,
      fraudScore: PropTypes.number.isRequired,
      status: PropTypes.string.isRequired,
    })
  ).isRequired,
  onRowClick: PropTypes.func,
};

TransactionTable.defaultProps = {
  onRowClick: null,
};

export default TransactionTable;

// Made with Bob