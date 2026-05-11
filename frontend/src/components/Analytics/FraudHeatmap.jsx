import React, { useState, useEffect } from 'react';
import { Tile, Loading } from '@carbon/react';
import { DonutChart } from '@carbon/charts-react';
import '@carbon/charts-react/styles.css';
import axios from 'axios';
import { API_ENDPOINTS } from '../../config/api';

/**
 * Fraud Country Heatmap Component
 * Visualizes suspicious transaction origins from database
 */
const FraudHeatmap = () => {
  const [loading, setLoading] = useState(true);
  const [heatmapData, setHeatmapData] = useState([]);

  useEffect(() => {
    fetchFraudData();
  }, []);

  const fetchFraudData = async () => {
    try {
      // Fetch transactions from database
      const response = await axios.get(API_ENDPOINTS.TRANSACTIONS);
      const transactions = response.data;
      
      // Group transactions by country and calculate fraud cases
      const countryMap = {};
      transactions.forEach(txn => {
        const country = txn.country || 'Unknown';
        if (!countryMap[country]) {
          countryMap[country] = {
            name: country,
            fraudCount: 0,
            totalCount: 0,
            totalFraudScore: 0
          };
        }
        countryMap[country].totalCount++;
        countryMap[country].totalFraudScore += (txn.fraudScore || 0);
        
        // Count as fraud case if score >= 0.5 or status is FLAGGED/REVIEW
        if (txn.fraudScore >= 0.5 || txn.status === 'FLAGGED' || txn.status === 'REVIEW') {
          countryMap[country].fraudCount++;
        }
      });
      
      // Convert to array and calculate risk scores
      const data = Object.values(countryMap).map(country => ({
        name: country.name,
        value: country.fraudCount,
        avgFraudScore: (country.totalFraudScore / country.totalCount) * 100
      }));
      
      setHeatmapData(data);
    } catch (error) {
      console.error('Error fetching fraud data:', error);
      setHeatmapData([]);
    } finally {
      setLoading(false);
    }
  };

  const getRiskLevel = (value) => {
    if (value >= 100) return { level: 'CRITICAL', color: '#da1e28' };
    if (value >= 50) return { level: 'HIGH', color: '#ff832b' };
    if (value >= 20) return { level: 'MEDIUM', color: '#ffdd00' };
    return { level: 'LOW', color: '#24a148' };
  };

  const getBarWidth = (value) => {
    const maxValue = Math.max(...heatmapData.map(d => d.value));
    return `${(value / maxValue) * 100}%`;
  };

  if (loading) {
    return <Loading description="Loading fraud heatmap..." />;
  }

  // Prepare data for donut chart - top 10 countries
  const topCountries = heatmapData
    .sort((a, b) => b.value - a.value)
    .slice(0, 10);

  const donutData = topCountries.map(item => ({
    group: item.name,
    value: item.value
  }));

  // Create color scale for donut chart
  const colorScale = {};
  topCountries.forEach(item => {
    const risk = getRiskLevel(item.value);
    colorScale[item.name] = risk.color;
  });

  const donutOptions = {
    title: 'Top Risk Countries',
    resizable: true,
    height: '400px',
    donut: {
      center: {
        label: 'Total Cases',
      },
      alignment: 'center',
    },
    toolbar: {
      enabled: false,
    },
    theme: 'g90',
    color: {
      scale: colorScale
    },
    legend: {
      alignment: 'center',
      truncation: {
        type: 'mid_line',
        threshold: 15,
        numCharacter: 12
      }
    }
  };

  return (
    <div>
      <DonutChart data={donutData} options={donutOptions} />
      
      {/* Risk Level Legend */}
      <div style={{
        marginTop: '1.5rem',
        padding: '1rem',
        backgroundColor: 'rgba(255, 255, 255, 0.05)',
        borderRadius: '4px',
        border: '1px solid rgba(255, 255, 255, 0.1)'
      }}>
        <h4 style={{ marginBottom: '0.75rem', fontSize: '0.875rem', fontWeight: '600' }}>Risk Level Guide</h4>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <div style={{
              width: '16px',
              height: '16px',
              backgroundColor: '#da1e28',
              borderRadius: '2px'
            }}></div>
            <span style={{ fontSize: '0.75rem', color: '#c6c6c6' }}>Critical (≥100)</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <div style={{
              width: '16px',
              height: '16px',
              backgroundColor: '#ff832b',
              borderRadius: '2px'
            }}></div>
            <span style={{ fontSize: '0.75rem', color: '#c6c6c6' }}>High (50-99)</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <div style={{
              width: '16px',
              height: '16px',
              backgroundColor: '#ffdd00',
              borderRadius: '2px'
            }}></div>
            <span style={{ fontSize: '0.75rem', color: '#c6c6c6' }}>Medium (20-49)</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <div style={{
              width: '16px',
              height: '16px',
              backgroundColor: '#24a148',
              borderRadius: '2px'
            }}></div>
            <span style={{ fontSize: '0.75rem', color: '#c6c6c6' }}>Low {'(<20)'}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FraudHeatmap;

// Made with Bob
