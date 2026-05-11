import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import {
  Tag,
  Loading,
  DataTable,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableHeader,
  TableBody,
  TableCell,
} from '@carbon/react';
import { GaugeChart } from '@carbon/charts-react';
import '@carbon/charts-react/styles.css';
import axios from 'axios';
import { API_ENDPOINTS } from '../../config/api';
import './Investigation.scss';

const Investigation = () => {
  const { txnId } = useParams();
  const [transaction, setTransaction] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (txnId) {
      fetchTransactionDetails();
    } else {
      setLoading(false);
      setError('No transaction ID provided');
    }
  }, [txnId]);

  const fetchTransactionDetails = async () => {
    try {
      const response = await axios.get(API_ENDPOINTS.INVESTIGATION_DETAILS(txnId));
      setTransaction(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching transaction details:', error);
      setError('Failed to load transaction details');
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="investigation-container">
        <Loading description="Loading transaction details..." withOverlay={false} />
      </div>
    );
  }

  if (error || !txnId) {
    return (
      <div className="investigation-container">
        <div className="error-message">
          <h2>Investigation Error</h2>
          <p>{error || 'Please select a transaction from the dashboard to view investigation details.'}</p>
          <p>Navigate to the <a href="/">Dashboard</a> to view transactions.</p>
        </div>
      </div>
    );
  }

  if (!transaction) {
    return (
      <div className="investigation-container">
        <div className="not-found-message">
          <h2>Transaction Not Found</h2>
          <p>The transaction with ID <strong>{txnId}</strong> could not be found.</p>
          <p>Navigate to the <a href="/">Dashboard</a> to view available transactions.</p>
        </div>
      </div>
    );
  }

  const fraudScoreValue = parseFloat(transaction.fraudScore) || 0;
  
  const gaugeData = [
    {
      group: 'value',
      value: fraudScoreValue * 100,
    },
  ];

  const gaugeOptions = {
    title: 'Fraud Risk Score',
    resizable: true,
    height: '250px',
    gauge: {
      type: 'semi',
      status: fraudScoreValue >= 0.8 ? 'danger' : fraudScoreValue >= 0.5 ? 'warning' : 'success',
    },
    toolbar: {
      enabled: false,
    },
  };

  const transactionHeaders = [
    { key: 'field', header: 'Field' },
    { key: 'value', header: 'Value' },
  ];

  const transactionRows = [
    { id: '1', field: 'Transaction ID', value: transaction.txnId },
    { id: '2', field: 'Customer ID', value: transaction.customerId },
    { id: '3', field: 'Timestamp', value: new Date(transaction.timestamp).toLocaleString() },
    {
      id: '4',
      field: 'Amount',
      value: `$${parseFloat(transaction.amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    },
    { id: '5', field: 'Merchant', value: transaction.merchant },
    { id: '6', field: 'Country', value: transaction.country },
    { id: '7', field: 'Device ID', value: transaction.deviceId },
    { id: '8', field: 'Payment Type', value: transaction.paymentType },
    {
      id: '9',
      field: 'Fraud Score',
      value: (
        <Tag type={fraudScoreValue >= 0.8 ? 'red' : fraudScoreValue >= 0.5 ? 'purple' : 'green'}>
          {(fraudScoreValue * 100).toFixed(1)}%
        </Tag>
      )
    },
    {
      id: '10',
      field: 'Status',
      value: (
        <Tag type={transaction.status === 'APPROVED' ? 'green' : transaction.status === 'REJECTED' ? 'red' : 'blue'}>
          {transaction.status}
        </Tag>
      )
    },
    {
      id: '11',
      field: 'Fraud Decision',
      value: transaction.fraudDecision ? (
        <Tag type={transaction.fraudDecision === 'APPROVE' ? 'green' : 'red'}>
          {transaction.fraudDecision}
        </Tag>
      ) : (
        <span style={{ color: '#8d8d8d' }}>N/A</span>
      )
    },
  ];

  const agentHeaders = [
    { key: 'agentName', header: 'Agent' },
    { key: 'score', header: 'Score' },
    { key: 'reason', header: 'Reason' },
  ];

  const agentRows = transaction.agentResults?.map((result, index) => {
    // Handle both riskScore (BigDecimal from backend) and score (number)
    const scoreValue = result.riskScore ? parseFloat(result.riskScore) : (result.score || 0);
    const reasonText = result.reasons && result.reasons.length > 0 ? result.reasons[0] : (result.reason || 'No reason provided');
    
    return {
      id: `agent-${index}`,
      agentName: result.agentName,
      score: (
        <Tag type={scoreValue >= 0.7 ? 'red' : scoreValue >= 0.4 ? 'purple' : 'green'}>
          {(scoreValue * 100).toFixed(0)}%
        </Tag>
      ),
      reason: reasonText,
    };
  }) || [];

  return (
    <div className="investigation-container">
      <div className="investigation-header">
        <h1>Investigation Details</h1>
        <p className="subtitle">Comprehensive fraud analysis for transaction {transaction.txnId}</p>
      </div>
      
      <div className="investigation-grid">
        <div className="left-column">
          <div className="info-tile">
            <h4>Transaction Information</h4>
            <DataTable rows={transactionRows} headers={transactionHeaders}>
              {({
                rows,
                headers,
                getTableProps,
                getHeaderProps,
                getRowProps,
                getTableContainerProps,
              }) => (
                <TableContainer {...getTableContainerProps()}>
                  <Table {...getTableProps()} size="lg">
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

          <div className="info-tile">
            <h4>Agent Analysis</h4>
            {agentRows.length > 0 ? (
              <DataTable rows={agentRows} headers={agentHeaders}>
                {({
                  rows,
                  headers,
                  getTableProps,
                  getHeaderProps,
                  getRowProps,
                  getTableContainerProps,
                }) => (
                  <TableContainer {...getTableContainerProps()}>
                    <Table {...getTableProps()} size="lg">
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
            ) : (
              <p className="no-data">No agent analysis available for this transaction.</p>
            )}
          </div>
        </div>

        <div className="right-column">
          <div className="gauge-tile">
            <GaugeChart data={gaugeData} options={gaugeOptions} />
          </div>

          <div className="risk-factors-tile">
            <h4>Risk Factors</h4>
            <div className="risk-factors-content">
              {transaction.riskFactors && transaction.riskFactors.length > 0 ? (
                transaction.riskFactors.map((factor, index) => (
                  <Tag key={index} type="red">
                    {factor}
                  </Tag>
                ))
              ) : (
                <p className="no-data">No risk factors identified.</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Investigation;

// Made with Bob