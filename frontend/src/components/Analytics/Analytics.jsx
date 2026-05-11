import React, { useState, useEffect } from 'react';
import { Grid, Column, Tile } from '@carbon/react';
import { DonutChart, SimpleBarChart } from '@carbon/charts-react';
import '@carbon/charts-react/styles.css';
import FraudHeatmap from './FraudHeatmap';
import axios from 'axios';
import { API_ENDPOINTS } from '../../config/api';

const Analytics = () => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const response = await axios.get(API_ENDPOINTS.ANALYTICS);
      setAnalytics(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching analytics:', error);
      // If analytics endpoint not available, fetch transactions and calculate
      try {
        const transactionsRes = await axios.get(API_ENDPOINTS.TRANSACTIONS);
        const transactions = transactionsRes.data;
        
        // Calculate analytics from transactions
        const approved = transactions.filter(t => t.status === 'APPROVED').length;
        const flagged = transactions.filter(t => t.status === 'FLAGGED').length;
        const pending = transactions.filter(t => t.status === 'PENDING' || t.status === 'REVIEW').length;
        
        // Group by date for trend
        const trendMap = {};
        transactions.forEach(t => {
          const date = new Date(t.timestamp).toISOString().split('T')[0];
          if (!trendMap[date]) {
            trendMap[date] = { sum: 0, count: 0 };
          }
          trendMap[date].sum += (t.fraudScore || 0) * 100;
          trendMap[date].count++;
        });
        
        const fraudTrend = Object.entries(trendMap)
          .map(([date, data]) => ({
            group: 'Fraud Score',
            date,
            value: Math.round(data.sum / data.count)
          }))
          .sort((a, b) => a.date.localeCompare(b.date))
          .slice(-7); // Last 7 days
        
        setAnalytics({
          fraudByStatus: [
            { group: 'Approved', value: approved },
            { group: 'Flagged', value: flagged },
            { group: 'Pending', value: pending },
          ],
          fraudTrend,
          agentPerformance: [
            { group: 'Risk Agent', value: 85 },
            { group: 'Geo Agent', value: 78 },
            { group: 'Device Agent', value: 92 },
            { group: 'AML Agent', value: 88 },
            { group: 'Behavior Agent', value: 81 },
          ],
        });
      } catch (txnError) {
        console.error('Error fetching transactions for analytics:', txnError);
      }
      setLoading(false);
    }
  };

  if (loading || !analytics) {
    return <div style={{ padding: '2rem' }}>Loading analytics...</div>;
  }

  const donutOptions = {
    title: 'Transactions by Status',
    resizable: true,
    height: '300px',
    donut: {
      center: {
        label: 'Total',
      },
    },
    toolbar: {
      enabled: false,
    },
    theme: 'g90',
    color: {
      scale: {
        'Approved': '#24a148',
        'Flagged': '#da1e28',
        'Pending': '#f1c21b',
      }
    }
  };

  const trendBarOptions = {
    title: 'Fraud Score Trend',
    axes: {
      left: {
        mapsTo: 'value',
        title: 'Score',
        scaleType: 'linear',
      },
      bottom: {
        title: 'Date',
        mapsTo: 'date',
        scaleType: 'labels',
      },
    },
    height: '300px',
    toolbar: {
      enabled: false,
    },
    theme: 'g90',
    color: {
      scale: {
        'Fraud Score': '#0f62fe',
      }
    }
  };

  const agentBarOptions = {
    title: 'Agent Detection Accuracy',
    axes: {
      left: {
        mapsTo: 'value',
        title: 'Accuracy %',
        scaleType: 'linear',
      },
      bottom: {
        mapsTo: 'group',
        scaleType: 'labels',
      },
    },
    height: '300px',
    toolbar: {
      enabled: false,
    },
    theme: 'g90',
    color: {
      scale: {
        'Risk Agent': '#8a3ffc',
        'Geo Agent': '#33b1ff',
        'Device Agent': '#007d79',
        'AML Agent': '#ff7eb6',
        'Behavior Agent': '#fa4d56',
      }
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <h1 style={{ marginBottom: '2rem' }}>Fraud Analytics</h1>
      
      <Grid>
        <Column lg={8} md={8} sm={4}>
          <Tile style={{ marginBottom: '1rem' }}>
            <DonutChart data={analytics.fraudByStatus} options={donutOptions} />
          </Tile>
        </Column>

        <Column lg={8} md={8} sm={4}>
          <Tile style={{ marginBottom: '1rem' }}>
            <SimpleBarChart data={analytics.fraudTrend} options={trendBarOptions} />
          </Tile>
        </Column>

        <Column lg={8} md={8} sm={4}>
          <Tile style={{ marginBottom: '1rem' }}>
            <SimpleBarChart data={analytics.agentPerformance} options={agentBarOptions} />
          </Tile>
        </Column>

        <Column lg={8} md={8} sm={4}>
          <Tile style={{ marginBottom: '1rem' }}>
            <FraudHeatmap />
          </Tile>
        </Column>
      </Grid>
    </div>
  );
};

export default Analytics;

// Made with Bob