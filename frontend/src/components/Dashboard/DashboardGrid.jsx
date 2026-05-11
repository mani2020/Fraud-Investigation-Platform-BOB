import React from 'react';
import PropTypes from 'prop-types';
import './DashboardGrid.scss';

/**
 * DashboardGrid - Responsive grid layout for dashboard widgets
 * 
 * Features:
 * - CSS Grid layout
 * - Responsive columns: mobile (1), tablet (2), desktop (3-4)
 * - Auto-fit grid items
 * - Proper gap spacing
 * - Card-based widgets
 */
const DashboardGrid = ({ children, columns, gap, className }) => {
  const gridClasses = [
    'dashboard-grid',
    columns && `dashboard-grid--cols-${columns}`,
    gap && `dashboard-grid--gap-${gap}`,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return <div className={gridClasses}>{children}</div>;
};

DashboardGrid.propTypes = {
  children: PropTypes.node.isRequired,
  columns: PropTypes.oneOf([1, 2, 3, 4, 'auto']),
  gap: PropTypes.oneOf(['sm', 'md', 'lg']),
  className: PropTypes.string,
};

DashboardGrid.defaultProps = {
  columns: 'auto',
  gap: 'md',
  className: '',
};

export default DashboardGrid;

// Made with Bob