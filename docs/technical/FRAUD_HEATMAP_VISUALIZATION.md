# Fraud Country Heatmap Visualization

Interactive world map showing suspicious transaction origins with color-coded risk levels.

---

## Overview

The Fraud Country Heatmap provides a visual representation of fraud transaction origins across the globe, helping investigators quickly identify high-risk geographic regions.

---

## Visual Representation

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                    GLOBAL FRAUD HEATMAP                                             │
│                    Suspicious Transaction Origins                                   │
└─────────────────────────────────────────────────────────────────────────────────────┘

                                    WORLD MAP
                                    
    ┌────────────────────────────────────────────────────────────────────────┐
    │                                                                        │
    │         North America                                                  │
    │         ░░░░░░░░░░░░                                                  │
    │         (Low Risk)                                                     │
    │                                                                        │
    │                                                                        │
    │                                                                        │
    │  Europe                                    Asia                        │
    │  ░░░░░░                                   ████████                    │
    │  (Low)                                    (Critical)                   │
    │                                                                        │
    │                                                                        │
    │         Africa                                                         │
    │         ████████                                                       │
    │         (Critical)                                                     │
    │                                                                        │
    │                    South America                                       │
    │                    ██████                                             │
    │                    (High)                                             │
    │                                                                        │
    │                                        Oceania                         │
    │                                        ░░░░░░                         │
    │                                        (Low)                           │
    │                                                                        │
    └────────────────────────────────────────────────────────────────────────┘

LEGEND:
████ Critical (≥100 cases)  - Dark Red (#750e13)
████ High (50-99 cases)     - Red (#da1e28)
████ Medium (20-49 cases)   - Orange (#ff832b)
████ Low (<20 cases)        - Yellow (#ffdd00)
░░░░ Minimal (<5 cases)     - Light Green (#d2f4ea)
```

---

## Risk Level Classification

### Critical Risk Countries (≥100 fraud cases)
```
┌─────────────────────────────────────────────────────────────┐
│ CRITICAL RISK COUNTRIES                                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 🔴 Nigeria (NGA)        145 cases                          │
│ 🔴 Russia (RUS)         132 cases                          │
│ 🔴 China (CHN)          124 cases                          │
│                                                             │
│ Common Fraud Types:                                         │
│ • Advance fee fraud                                         │
│ • Credit card fraud                                         │
│ • Identity theft                                            │
│ • Wire transfer scams                                       │
│                                                             │
│ Recommended Actions:                                        │
│ ✓ Enhanced verification required                           │
│ ✓ Additional authentication steps                          │
│ ✓ Manual review for all transactions                       │
│ ✓ Customer contact verification                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### High Risk Countries (50-99 fraud cases)
```
┌─────────────────────────────────────────────────────────────┐
│ HIGH RISK COUNTRIES                                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 🟠 Brazil (BRA)         98 cases                           │
│ 🟠 India (IND)          85 cases                           │
│ 🟠 Indonesia (IDN)      76 cases                           │
│ 🟠 Pakistan (PAK)       68 cases                           │
│ 🟠 Vietnam (VNM)        62 cases                           │
│                                                             │
│ Recommended Actions:                                        │
│ ✓ Increased monitoring                                     │
│ ✓ Transaction limits                                       │
│ ✓ Velocity checks                                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Medium Risk Countries (20-49 fraud cases)
```
┌─────────────────────────────────────────────────────────────┐
│ MEDIUM RISK COUNTRIES                                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 🟡 Mexico (MEX)         45 cases                           │
│ 🟡 Turkey (TUR)         42 cases                           │
│ 🟡 Egypt (EGY)          38 cases                           │
│ 🟡 Philippines (PHL)    35 cases                           │
│ 🟡 Thailand (THA)       32 cases                           │
│                                                             │
│ Recommended Actions:                                        │
│ ✓ Standard verification                                    │
│ ✓ Pattern monitoring                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Low Risk Countries (<20 fraud cases)
```
┌─────────────────────────────────────────────────────────────┐
│ LOW RISK COUNTRIES                                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 🟢 United States (USA)  15 cases                           │
│ 🟢 United Kingdom (GBR) 12 cases                           │
│ 🟢 Germany (DEU)        10 cases                           │
│ 🟢 France (FRA)          9 cases                           │
│ 🟢 Canada (CAN)          8 cases                           │
│                                                             │
│ Recommended Actions:                                        │
│ ✓ Normal processing                                        │
│ ✓ Standard checks                                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Interactive Features

### 1. Hover Tooltip
```
┌─────────────────────────────────────┐
│ Nigeria                             │
│ Fraud Cases: 145                    │
│ Risk Level: CRITICAL                │
│                                     │
│ Recent Trends:                      │
│ • +15% from last month             │
│ • Peak hours: 2-4 AM UTC           │
│ • Common merchants: Unknown         │
└─────────────────────────────────────┘
```

### 2. Click for Details
```
┌─────────────────────────────────────────────────────────────┐
│ NIGERIA - DETAILED FRAUD ANALYSIS                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Total Fraud Cases: 145                                      │
│ Fraud Rate: 8.5%                                           │
│ Total Amount Lost: $2,450,000                              │
│                                                             │
│ Fraud Types:                                               │
│ • Advance Fee Fraud: 45%                                   │
│ • Credit Card Fraud: 30%                                   │
│ • Identity Theft: 15%                                      │
│ • Wire Transfer: 10%                                       │
│                                                             │
│ Time Distribution:                                         │
│ • 2-4 AM: 35%                                             │
│ • 10 PM-12 AM: 25%                                        │
│ • 6-8 PM: 20%                                             │
│ • Other: 20%                                              │
│                                                             │
│ Top Targeted Merchants:                                    │
│ 1. Unknown Vendors (40%)                                   │
│ 2. Crypto Exchanges (25%)                                  │
│ 3. Online Gambling (20%)                                   │
│ 4. Gift Card Sellers (15%)                                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3. Filter Options
```
┌─────────────────────────────────────────────────────────────┐
│ FILTERS                                                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Time Period:  [Last 30 Days ▼]                            │
│ Risk Level:   [All ▼] [Critical] [High] [Medium] [Low]    │
│ Fraud Type:   [All Types ▼]                               │
│ Amount Range: [$0 - $100,000]                             │
│                                                             │
│ [Apply Filters]  [Reset]                                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Implementation

### React Component Structure

```javascript
import React, { useState, useEffect } from 'react';
import { ChoroplethChart } from '@carbon/charts-react';
import { Tile, Toggle, Select } from '@carbon/react';

const FraudHeatmap = () => {
  const [heatmapData, setHeatmapData] = useState([]);
  const [timeRange, setTimeRange] = useState('30days');
  const [riskFilter, setRiskFilter] = useState('all');

  // Fetch fraud data by country
  useEffect(() => {
    fetchFraudData();
  }, [timeRange, riskFilter]);

  const fetchFraudData = async () => {
    const response = await fetch(`/api/analytics/fraud-by-country?range=${timeRange}`);
    const data = await response.json();
    setHeatmapData(transformData(data));
  };

  const transformData = (data) => {
    return data.map(item => ({
      group: item.countryCode, // ISO 3166-1 alpha-3
      value: item.fraudCount
    }));
  };

  const chartOptions = {
    title: 'Global Fraud Heatmap',
    height: '600px',
    choropleth: {
      colorLegend: {
        title: 'Fraud Cases',
        type: 'linear'
      }
    },
    color: {
      gradient: {
        colors: ['#d2f4ea', '#ffdd00', '#ff832b', '#da1e28', '#750e13']
      }
    }
  };

  return (
    <Tile>
      <ChoroplethChart data={heatmapData} options={chartOptions} />
    </Tile>
  );
};
```

---

## Data Structure

### API Response Format

```json
{
  "timeRange": "30days",
  "totalFraudCases": 1247,
  "countries": [
    {
      "countryCode": "NGA",
      "countryName": "Nigeria",
      "fraudCount": 145,
      "fraudRate": 8.5,
      "totalAmount": 2450000,
      "riskLevel": "CRITICAL",
      "fraudTypes": {
        "advanceFee": 45,
        "creditCard": 30,
        "identityTheft": 15,
        "wireTransfer": 10
      },
      "timeDistribution": {
        "2-4AM": 35,
        "10PM-12AM": 25,
        "6-8PM": 20,
        "other": 20
      },
      "topMerchants": [
        { "name": "Unknown Vendors", "percentage": 40 },
        { "name": "Crypto Exchanges", "percentage": 25 },
        { "name": "Online Gambling", "percentage": 20 },
        { "name": "Gift Card Sellers", "percentage": 15 }
      ]
    }
  ]
}
```

---

## Color Gradient Scale

```
Value Range    Color       Hex Code    Risk Level
───────────────────────────────────────────────────
0-5           Light Green  #d2f4ea    Minimal
5-20          Yellow       #ffdd00    Low
20-50         Orange       #ff832b    Medium
50-100        Red          #da1e28    High
100+          Dark Red     #750e13    Critical
```

---

## Top 10 High-Risk Countries Table

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ TOP 10 HIGH-RISK COUNTRIES                                                  │
├──────┬─────────────────────┬──────────────┬─────────────┬──────────────────┤
│ Rank │ Country             │ Fraud Cases  │ Fraud Rate  │ Risk Level       │
├──────┼─────────────────────┼──────────────┼─────────────┼──────────────────┤
│  1   │ 🇳🇬 Nigeria         │     145      │    8.5%     │ 🔴 CRITICAL     │
│  2   │ 🇷🇺 Russia          │     132      │    7.8%     │ 🔴 CRITICAL     │
│  3   │ 🇨🇳 China           │     124      │    7.2%     │ 🔴 CRITICAL     │
│  4   │ 🇧🇷 Brazil          │      98      │    5.8%     │ 🟠 HIGH         │
│  5   │ 🇮🇳 India           │      85      │    5.0%     │ 🟠 HIGH         │
│  6   │ 🇮🇩 Indonesia       │      76      │    4.5%     │ 🟠 HIGH         │
│  7   │ 🇵🇰 Pakistan        │      68      │    4.0%     │ 🟠 HIGH         │
│  8   │ 🇻🇳 Vietnam         │      62      │    3.7%     │ 🟠 HIGH         │
│  9   │ 🇲🇽 Mexico          │      45      │    2.7%     │ 🟡 MEDIUM       │
│ 10   │ 🇹🇷 Turkey          │      42      │    2.5%     │ 🟡 MEDIUM       │
└──────┴─────────────────────┴──────────────┴─────────────┴──────────────────┘
```

---

## Integration with Dashboard

### Adding to Analytics Screen

```javascript
// In Analytics.jsx
import FraudHeatmap from './FraudHeatmap';

const Analytics = () => {
  return (
    <div className="analytics-container">
      <h2>Fraud Analytics Dashboard</h2>
      
      {/* Existing charts */}
      <Grid>
        <Column lg={16}>
          <FraudHeatmap />
        </Column>
        
        <Column lg={8}>
          <FraudByCountryChart />
        </Column>
        
        <Column lg={8}>
          <FraudTrendChart />
        </Column>
      </Grid>
    </div>
  );
};
```

---

## Use Cases

### 1. Risk Assessment
- Quickly identify high-risk geographic regions
- Adjust fraud detection rules by country
- Set transaction limits based on origin

### 2. Pattern Recognition
- Spot emerging fraud hotspots
- Track fraud migration patterns
- Identify coordinated fraud campaigns

### 3. Resource Allocation
- Focus investigation resources on high-risk areas
- Prioritize manual reviews by country
- Allocate fraud prevention budgets

### 4. Compliance & Reporting
- Generate geographic fraud reports
- Track AML compliance by region
- Document risk assessment procedures

---

## Performance Considerations

### Data Loading
- Cache country data for 5 minutes
- Lazy load detailed country information
- Progressive rendering for large datasets

### Optimization
```javascript
// Debounce filter changes
const debouncedFilter = useDebounce(filterValue, 300);

// Memoize expensive calculations
const processedData = useMemo(() => {
  return transformData(rawData);
}, [rawData]);

// Virtual scrolling for country list
<VirtualList
  items={countries}
  itemHeight={50}
  renderItem={renderCountryRow}
/>
```

---

## Accessibility

- **Keyboard Navigation**: Tab through countries
- **Screen Reader**: Country names and fraud counts announced
- **Color Blind Friendly**: Patterns in addition to colors
- **High Contrast**: Alternative color scheme available

---

## Future Enhancements

1. **Real-time Updates**: WebSocket for live fraud data
2. **Drill-down**: Click country for detailed analysis
3. **Time-lapse**: Animate fraud patterns over time
4. **Comparison Mode**: Compare two time periods
5. **Export**: Download heatmap as image/PDF
6. **Alerts**: Notify when country risk level changes

---

## Related Documentation

- [Fraud Analytics Dashboard](DASHBOARD_SCREENS.md#screen-3-fraud-analytics)
- [Fraud Flow Technical](FRAUD_FLOW_TECHNICAL.md)
- [Investigation Timeline](FRAUD_INVESTIGATION_TIMELINE.md)

---

**Made with ❤️ using IBM Carbon Charts**