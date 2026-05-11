# Dashboard Screen Specifications

Complete specification for the three main dashboard screens with IBM Carbon Design System.

---

## Screen 1: Live Transactions Monitor

### Purpose
Real-time monitoring of all payment transactions with fraud detection status.

### Layout
- Full-width Carbon DataTable
- Auto-refresh every 5 seconds
- Sortable and filterable columns

### Columns

| Column | Description | Format |
|--------|-------------|--------|
| Transaction ID | Unique identifier | TXN-XXXX |
| Customer ID | Customer identifier | CUST-XXXX |
| Amount | Transaction amount | $X,XXX.XX |
| Merchant | Merchant name | Text |
| Country | Transaction country | 2-letter code |
| Timestamp | Transaction time | MMM DD, YYYY HH:MM |
| Status | Processing status | Badge |
| Actions | View details button | Button |

### Status Colors

```javascript
const statusColors = {
  'Approved': 'green',    // ✅ Green badge
  'Fraud': 'red',         // ❌ Red badge
  'Pending': 'blue',      // 🔵 Blue badge
  'Review': 'yellow'      // ⚠️ Yellow badge
};
```

### Visual Design

```
┌─────────────────────────────────────────────────────────────────┐
│  Live Transactions Monitor                    🔄 Auto-refresh   │
├─────────────────────────────────────────────────────────────────┤
│ TxnID    Customer  Amount    Merchant  Country  Time    Status  │
├─────────────────────────────────────────────────────────────────┤
│ TXN-001  CUST-123  $5,000   Amazon    US       14:30   ✅ Approved│
│ TXN-002  CUST-456  $50,000  Unknown   NG       14:31   ❌ Fraud  │
│ TXN-003  CUST-789  $1,200   Walmart   US       14:32   ✅ Approved│
│ TXN-004  CUST-234  $25,000  Crypto    RU       14:33   ❌ Fraud  │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Code

```javascript
// Screen 1: Live Transactions
import { DataTable, Tag } from '@carbon/react';

const getStatusTag = (fraudDecision) => {
  if (fraudDecision === 'APPROVE') {
    return <Tag type="green">Approved</Tag>;
  } else if (fraudDecision === 'REJECT') {
    return <Tag type="red">Fraud</Tag>;
  } else if (fraudDecision === 'REVIEW') {
    return <Tag type="yellow">Review</Tag>;
  } else {
    return <Tag type="blue">Pending</Tag>;
  }
};

const headers = [
  { key: 'txnId', header: 'Transaction ID' },
  { key: 'customerId', header: 'Customer ID' },
  { key: 'amount', header: 'Amount' },
  { key: 'merchant', header: 'Merchant' },
  { key: 'country', header: 'Country' },
  { key: 'timestamp', header: 'Time' },
  { key: 'status', header: 'Status' },
  { key: 'actions', header: 'Actions' },
];

const rows = transactions.map(txn => ({
  id: txn.id,
  txnId: txn.txnId,
  customerId: txn.customerId,
  amount: formatCurrency(txn.amount),
  merchant: txn.merchant,
  country: txn.country,
  timestamp: formatTime(txn.timestamp),
  status: getStatusTag(txn.fraudDecision),
  actions: <Button size="sm" onClick={() => navigate(`/investigation/${txn.txnId}`)}>
    View Details
  </Button>
}));
```

---

## Screen 2: Investigation Details

### Purpose
Deep dive into a specific transaction's fraud analysis with AI explanations.

### Layout
Three-section vertical layout:
1. **Risk Score Section** (Top)
2. **Agent Outputs Section** (Middle)
3. **AI Explanation Section** (Bottom)

### Section 1: Risk Score

```
┌─────────────────────────────────────────────────────────────────┐
│  Transaction: TXN-002                                           │
│  Customer: CUST-456 | Amount: $50,000 | Merchant: Unknown      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│              ╭─────────────────╮                               │
│              │                 │                               │
│              │    FRAUD        │                               │
│              │    SCORE        │                               │
│              │                 │                               │
│              │      85.5       │    ← Gauge Chart             │
│              │                 │                               │
│              │   CRITICAL      │                               │
│              │                 │                               │
│              ╰─────────────────╯                               │
│                                                                 │
│  Decision: ❌ REJECT                                           │
│  Confidence: 92%                                               │
└─────────────────────────────────────────────────────────────────┘
```

### Section 2: Agent Outputs

```
┌─────────────────────────────────────────────────────────────────┐
│  Agent Analysis Results                                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ▼ Risk Agent                                    Score: 85.0   │
│     • High transaction amount ($50,000)                        │
│     • Exceeds customer's average by 400%                       │
│     • Decision: REJECT                                         │
│                                                                 │
│  ▼ Geo Agent                                     Score: 90.0   │
│     • High-risk country (Nigeria)                              │
│     • Impossible travel detected                               │
│     • Decision: REJECT                                         │
│                                                                 │
│  ▼ Device Agent                                  Score: 75.0   │
│     • Unknown device fingerprint                               │
│     • No previous transactions from this device                │
│     • Decision: REVIEW                                         │
│                                                                 │
│  ▼ AML Agent                                     Score: 60.0   │
│     • Merchant not on watchlist                                │
│     • No suspicious patterns detected                          │
│     • Decision: REVIEW                                         │
│                                                                 │
│  ▼ Behavior Agent                                Score: 80.0   │
│     • Unusual transaction time (3:00 AM)                       │
│     • Merchant category deviation                              │
│     • Decision: REJECT                                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Section 3: AI Explanation

```
┌─────────────────────────────────────────────────────────────────┐
│  Explainable AI Analysis                                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  🔴 HIGH FRAUD RISK DETECTED                                   │
│                                                                 │
│  This transaction has been flagged as fraudulent with a        │
│  confidence score of 85.5/100. Multiple fraud indicators       │
│  were detected across different analysis agents.               │
│                                                                 │
│  KEY RISK FACTORS:                                             │
│                                                                 │
│  1. Geographic Risk (Impact: 35%)                              │
│     Transaction originated from Nigeria, a high-risk country   │
│     for payment fraud. Customer's previous transactions were   │
│     all from the United States.                                │
│                                                                 │
│  2. Transaction Amount (Impact: 30%)                           │
│     Amount of $50,000 is 400% higher than customer's average   │
│     transaction of $1,250. This represents a significant       │
│     deviation from normal spending patterns.                   │
│                                                                 │
│  3. Device Fingerprint (Impact: 20%)                           │
│     Transaction was initiated from an unrecognized device.     │
│     Customer typically uses 2 known devices for payments.      │
│                                                                 │
│  4. Behavioral Anomaly (Impact: 15%)                           │
│     Transaction occurred at 3:00 AM, outside customer's        │
│     typical transaction window of 9:00 AM - 6:00 PM.          │
│                                                                 │
│  RECOMMENDATION: REJECT                                        │
│  This transaction should be blocked and the customer should    │
│  be contacted for verification.                                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Code

```javascript
// Screen 2: Investigation Details
import { Tile, Accordion, AccordionItem, ProgressBar } from '@carbon/react';
import { GaugeChart } from '@carbon/charts-react';

const Investigation = () => {
  return (
    <div className="investigation-container">
      {/* Risk Score Section */}
      <Tile className="risk-score-section">
        <h3>Fraud Risk Assessment</h3>
        <GaugeChart
          data={[{ group: 'score', value: 85.5 }]}
          options={{
            title: 'Fraud Score',
            gauge: {
              type: 'semi',
              status: 'danger'
            }
          }}
        />
        <div className="decision-info">
          <Tag type="red">REJECT</Tag>
          <p>Confidence: 92%</p>
        </div>
      </Tile>

      {/* Agent Outputs Section */}
      <Tile className="agent-outputs-section">
        <h3>Agent Analysis Results</h3>
        <Accordion>
          <AccordionItem title="Risk Agent - Score: 85.0">
            <ul>
              <li>High transaction amount ($50,000)</li>
              <li>Exceeds customer's average by 400%</li>
              <li>Decision: REJECT</li>
            </ul>
          </AccordionItem>
          {/* More agents... */}
        </Accordion>
      </Tile>

      {/* AI Explanation Section */}
      <Tile className="ai-explanation-section">
        <h3>Explainable AI Analysis</h3>
        <div className="explanation-content">
          <h4>🔴 HIGH FRAUD RISK DETECTED</h4>
          <p>This transaction has been flagged as fraudulent...</p>
          
          <h5>KEY RISK FACTORS:</h5>
          <ol>
            <li>
              <strong>Geographic Risk (Impact: 35%)</strong>
              <p>Transaction originated from Nigeria...</p>
            </li>
            {/* More factors... */}
          </ol>
          
          <div className="recommendation">
            <strong>RECOMMENDATION: REJECT</strong>
            <p>This transaction should be blocked...</p>
          </div>
        </div>
      </Tile>
    </div>
  );
};
```

---

## Screen 3: Fraud Analytics

### Purpose
Visual analytics dashboard showing fraud patterns and trends.

### Layout
Three-chart grid layout with Carbon Charts.

### Chart 1: Fraud by Country (Top)

```
┌─────────────────────────────────────────────────────────────────┐
│  Fraud Transactions by Country                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Nigeria     ████████████████████████████ 45                   │
│  Russia      ████████████████████ 32                           │
│  China       ███████████████ 24                                │
│  Brazil      ████████████ 18                                   │
│  India       ██████████ 15                                     │
│  USA         ████ 6                                            │
│  UK          ███ 4                                             │
│  Germany     ██ 3                                              │
│                                                                 │
│              0    10    20    30    40    50                   │
│                   Number of Fraud Cases                        │
└─────────────────────────────────────────────────────────────────┘
```

### Chart 2: Fraud Trend (Middle)

```
┌─────────────────────────────────────────────────────────────────┐
│  Fraud Rate Trend (Last 30 Days)                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  5% ┤                                            ╭──╮          │
│     │                                    ╭───╮   │  │          │
│  4% ┤                            ╭───╮   │   │   │  │          │
│     │                    ╭───╮   │   │   │   │   │  │          │
│  3% ┤            ╭───╮   │   │   │   │   │   │   │  │          │
│     │    ╭───╮   │   │   │   │   │   │   │   │   │  │          │
│  2% ┤────┤   │───┤   │───┤   │───┤   │───┤   │───┤  │────      │
│     │    │   │   │   │   │   │   │   │   │   │   │  │          │
│  1% ┤    │   │   │   │   │   │   │   │   │   │   │  │          │
│     │    │   │   │   │   │   │   │   │   │   │   │  │          │
│  0% └────┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴──┴────      │
│      May  May  May  May  May  May  May  May  May  May  May     │
│       1    5    9   13   17   21   25   29    2    6   10      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Chart 3: Fraud by Merchant (Bottom)

```
┌─────────────────────────────────────────────────────────────────┐
│  Top Merchants with Fraud Cases                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│                    ╭────╮                                       │
│                    │    │                                       │
│                    │ 38 │                                       │
│          ╭────╮    │    │                                       │
│          │    │    │    │                                       │
│          │ 25 │    │    │          ╭────╮                      │
│          │    │    │    │          │    │                      │
│  ╭────╮  │    │    │    │  ╭────╮  │ 12 │  ╭────╮            │
│  │    │  │    │    │    │  │    │  │    │  │    │            │
│  │ 15 │  │    │    │    │  │ 18 │  │    │  │  8 │            │
│  │    │  │    │    │    │  │    │  │    │  │    │            │
│  └────┘  └────┘    └────┘  └────┘  └────┘  └────┘            │
│  Unknown Crypto   Online  Gift    Forex   Travel              │
│  Vendor  Exchange Gambling Cards  Trading Booking             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Code

```javascript
// Screen 3: Fraud Analytics
import { Tile, Grid, Column } from '@carbon/react';
import { BarChart, LineChart } from '@carbon/charts-react';

const FraudAnalytics = () => {
  // Chart 1: Fraud by Country
  const countryData = [
    { group: 'Nigeria', value: 45 },
    { group: 'Russia', value: 32 },
    { group: 'China', value: 24 },
    { group: 'Brazil', value: 18 },
    { group: 'India', value: 15 },
    { group: 'USA', value: 6 },
    { group: 'UK', value: 4 },
    { group: 'Germany', value: 3 },
  ];

  const countryOptions = {
    title: 'Fraud Transactions by Country',
    axes: {
      left: { mapsTo: 'group', scaleType: 'labels' },
      bottom: { mapsTo: 'value', title: 'Number of Fraud Cases' }
    },
    height: '400px',
    bars: { color: '#da1e28' } // Red for fraud
  };

  // Chart 2: Fraud Trend
  const trendData = [
    { group: 'Fraud Rate', date: '2026-05-01', value: 2.1 },
    { group: 'Fraud Rate', date: '2026-05-05', value: 2.5 },
    { group: 'Fraud Rate', date: '2026-05-09', value: 3.2 },
    { group: 'Fraud Rate', date: '2026-05-13', value: 3.8 },
    { group: 'Fraud Rate', date: '2026-05-17', value: 4.1 },
    { group: 'Fraud Rate', date: '2026-05-21', value: 3.9 },
    { group: 'Fraud Rate', date: '2026-05-25', value: 4.5 },
    { group: 'Fraud Rate', date: '2026-05-29', value: 4.8 },
  ];

  const trendOptions = {
    title: 'Fraud Rate Trend (Last 30 Days)',
    axes: {
      bottom: { title: 'Date', mapsTo: 'date', scaleType: 'time' },
      left: { mapsTo: 'value', title: 'Fraud Rate (%)' }
    },
    height: '400px',
    color: { scale: { 'Fraud Rate': '#da1e28' } }
  };

  // Chart 3: Fraud by Merchant
  const merchantData = [
    { group: 'Unknown Vendor', value: 15 },
    { group: 'Crypto Exchange', value: 25 },
    { group: 'Online Gambling', value: 38 },
    { group: 'Gift Cards', value: 18 },
    { group: 'Forex Trading', value: 12 },
    { group: 'Travel Booking', value: 8 },
  ];

  const merchantOptions = {
    title: 'Top Merchants with Fraud Cases',
    axes: {
      left: { mapsTo: 'value', title: 'Fraud Cases' },
      bottom: { mapsTo: 'group', scaleType: 'labels' }
    },
    height: '400px',
    bars: { color: '#da1e28' }
  };

  return (
    <div className="analytics-container">
      <h2>Fraud Analytics Dashboard</h2>
      
      <Grid>
        <Column lg={16}>
          <Tile>
            <BarChart data={countryData} options={countryOptions} />
          </Tile>
        </Column>
        
        <Column lg={16}>
          <Tile>
            <LineChart data={trendData} options={trendOptions} />
          </Tile>
        </Column>
        
        <Column lg={16}>
          <Tile>
            <BarChart data={merchantData} options={merchantOptions} />
          </Tile>
        </Column>
      </Grid>
    </div>
  );
};

export default FraudAnalytics;
```

---

## Color Scheme

### Status Colors
```css
.status-approved {
  background-color: #24a148; /* Green */
  color: white;
}

.status-fraud {
  background-color: #da1e28; /* Red */
  color: white;
}

.status-pending {
  background-color: #0f62fe; /* Blue */
  color: white;
}

.status-review {
  background-color: #f1c21b; /* Yellow */
  color: black;
}
```

### Risk Score Colors
```css
.risk-low {
  color: #24a148; /* Green - Score < 30 */
}

.risk-medium {
  color: #f1c21b; /* Yellow - Score 30-49 */
}

.risk-high {
  color: #ff832b; /* Orange - Score 50-69 */
}

.risk-critical {
  color: #da1e28; /* Red - Score >= 70 */
}
```

---

## Navigation Flow

```
Dashboard Home
    │
    ├─→ Screen 1: Live Transactions
    │       │
    │       └─→ Click "View Details" → Screen 2: Investigation
    │
    ├─→ Screen 2: Investigation Details
    │       │
    │       └─→ Back to Transactions
    │
    └─→ Screen 3: Fraud Analytics
            │
            └─→ Click on chart elements for drill-down
```

---

## Data Refresh Strategy

- **Screen 1**: Auto-refresh every 5 seconds
- **Screen 2**: Load once, manual refresh button
- **Screen 3**: Refresh every 30 seconds

---

## Responsive Design

All screens adapt to different screen sizes:
- **Desktop**: Full layout as shown
- **Tablet**: Stacked charts, scrollable tables
- **Mobile**: Single column, simplified views

---

**Made with ❤️ using IBM Carbon Design System**