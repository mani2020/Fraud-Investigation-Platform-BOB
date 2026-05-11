import React from 'react';
import './StatusBadge.scss';

/**
 * Premium status badge with glow effects and animations
 */
const StatusBadge = ({ 
  status, 
  type = 'default', // success, warning, danger, info, pending
  glow = false,
  pulse = false,
  className = '',
  children,
  ...props 
}) => {
  const badgeClasses = [
    'status-badge',
    `status-badge--${type}`,
    glow && 'status-badge--glow',
    pulse && 'status-badge--pulse',
    className
  ].filter(Boolean).join(' ');

  return (
    <span className={badgeClasses} {...props}>
      {children || status}
    </span>
  );
};

export default StatusBadge;

// Made with Bob
