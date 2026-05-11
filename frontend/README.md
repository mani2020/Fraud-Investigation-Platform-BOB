# Fraud Investigation Dashboard

Real-time fraud investigation dashboard built with React and IBM Carbon Design System.

## Features

- 📊 **Live Transaction Monitoring** - Real-time transaction feed with fraud scores
- 🚨 **Fraud Alerts** - Critical alerts dashboard with severity filtering
- 🔍 **Investigation Details** - Detailed fraud analysis and agent results
- 📈 **Risk Score Visualization** - Interactive charts and gauges
- 📉 **Fraud Analytics** - Trends, statistics, and insights

## Tech Stack

- **React 18** - UI framework
- **IBM Carbon Design System** - Enterprise UI components
- **Carbon Charts** - Data visualization
- **Vite** - Build tool
- **React Router** - Navigation
- **Axios** - API client

## Quick Start

### Prerequisites
- Node.js 18+
- npm or yarn
- Backend API running on http://localhost:8080

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
npm run dev
```

Dashboard will be available at http://localhost:5174

### Build

```bash
npm run build
```

## Project Structure

```
frontend/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── Dashboard/       # Main dashboard layout
│   │   ├── Transactions/    # Transaction monitoring
│   │   ├── Alerts/          # Fraud alerts
│   │   ├── Investigation/   # Investigation details
│   │   ├── Analytics/       # Charts and analytics
│   │   ├── layout/          # Layout components (Navbar, Sidebar)
│   │   └── common/          # Shared components (CustomPagination, etc.)
│   ├── services/            # API services
│   │   ├── api.js          # Axios configuration
│   │   ├── transactionService.js
│   │   ├── alertService.js
│   │   └── analyticsService.js
│   ├── hooks/               # Custom React hooks
│   │   ├── useTransactions.js
│   │   ├── useAlerts.js
│   │   └── usePolling.js
│   ├── utils/               # Utility functions
│   │   ├── formatters.js
│   │   └── constants.js
│   ├── App.jsx              # Main app component
│   ├── main.jsx             # Entry point
│   └── index.scss           # Global styles
├── public/                  # Static assets
├── index.html              # HTML template
├── vite.config.js          # Vite configuration
└── package.json            # Dependencies
```

## Key Components

### 1. Dashboard Layout
Main layout with Carbon UI Shell, header, and side navigation.

### 2. Transaction Monitor
- Real-time transaction table
- Fraud score indicators
- Status badges (PENDING, PROCESSED, APPROVED, REJECTED)
- Filtering and sorting

### 3. Fraud Alerts
- Alert cards with severity levels
- Critical/High/Medium/Low filtering
- Acknowledge functionality
- Real-time updates (5-second polling)

### 4. Investigation Panel
- Transaction details
- Agent analysis results
- Risk factors breakdown
- Explainable AI insights

### 5. Risk Score Visualization
- Gauge charts for fraud scores
- Agent contribution breakdown
- Historical trends
- Score distribution

### 6. Analytics Dashboard
- Fraud rate trends
- Decision distribution (pie chart)
- Alert statistics
- Performance metrics

## API Integration

### Base URL
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

### Endpoints Used

```javascript
// Transactions
GET    /api/transactions
GET    /api/transactions/{txnId}

// Alerts
GET    /api/alerts
GET    /api/alerts/critical
GET    /api/alerts/unacknowledged
PUT    /api/alerts/{alertId}/acknowledge
GET    /api/alerts/stats
```

## Carbon Design Components Used

- **UI Shell** - Application frame
- **DataTable** - Transaction and alert lists
- **Tile** - Card layouts
- **Tag** - Status indicators
- **Button** - Actions
- **Modal** - Investigation details
- **Notification** - Toast messages
- **Loading** - Skeleton states
- **Tabs** - Content organization
- **Accordion** - Collapsible sections

## Carbon Charts Used

- **GaugeChart** - Fraud score visualization
- **DonutChart** - Decision distribution
- **LineChart** - Trend analysis
- **BarChart** - Agent comparison

## Styling

Uses Carbon Design System's SCSS themes:

```scss
@use '@carbon/react';
@use '@carbon/charts/styles';
```

## Real-time Updates

Implements polling strategy for live updates:

```javascript
// Poll every 5 seconds
useEffect(() => {
  const interval = setInterval(() => {
    fetchAlerts();
    fetchTransactions();
  }, 5000);
  
  return () => clearInterval(interval);
}, []);
```

## State Management

Uses React hooks for state management:
- `useState` - Component state
- `useEffect` - Side effects
- `useContext` - Global state (if needed)
- Custom hooks for data fetching

## Error Handling

- API error boundaries
- Loading states
- Empty states
- Error notifications

## Performance Optimization

- Lazy loading for routes
- Memoization for expensive computations
- Debouncing for search/filter
- Virtual scrolling for large lists

## Accessibility

- WCAG 2.1 AA compliant (Carbon Design)
- Keyboard navigation
- Screen reader support
- Focus management

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Environment Variables

Create `.env` file:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_POLLING_INTERVAL=5000
```

## Testing

```bash
npm run test
```

## Deployment

```bash
npm run build
# Deploy dist/ folder to web server
```

## Troubleshooting

### API Connection Issues
- Ensure backend is running on port 8080
- Check CORS configuration
- Verify proxy settings in vite.config.js

### Build Errors
- Clear node_modules and reinstall
- Check Node.js version (18+)
- Verify all dependencies are installed

## Recent Updates

### Pagination & UI Improvements (2026-05-11)
- ✅ Fixed data alignment issues on Fraud Alerts and Transaction Monitor pages
- ✅ Implemented custom pagination component matching design standards
- ✅ Added search functionality with real-time filtering
- ✅ Fixed View button navigation to investigation page
- ✅ Mobile-responsive design improvements
- ✅ Enhanced table styling and layout

See [FRAUD_ALERTS_PAGINATION_FIX.md](../docs/development/FRAUD_ALERTS_PAGINATION_FIX.md) for details.

## Future Enhancements

- [ ] WebSocket for real-time updates
- [ ] Advanced filtering and search
- [ ] Export functionality (CSV, PDF)
- [ ] User authentication
- [ ] Role-based access control
- [x] Mobile responsive design (Completed)
- [ ] Offline support
- [ ] Performance monitoring
- [ ] A/B testing integration

## Contributing

1. Follow Carbon Design System guidelines
2. Use functional components with hooks
3. Write clean, documented code
4. Test all features
5. Ensure accessibility compliance

## License

Proprietary - Internal Use Only

---

**Made with ❤️ using IBM Carbon Design System**