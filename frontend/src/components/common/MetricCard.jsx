import React from 'react';
import './MetricCard.scss';

/**
 * Premium metric card with gradient text and glow effects
 */
const MetricCard = ({
  value,
  label,
  change,
  changeType = 'neutral', // positive, negative, neutral
  icon: Icon,
  variant = 'default', // default, purple, cyan, danger, success
  className = '',
  ...props
}) => {
  const cardClasses = [
    'metric-card',
    `metric-card--${variant}`,
    className
  ].filter(Boolean).join(' ');

  const changeClasses = [
    'metric-card__change',
    changeType && `metric-card__change--${changeType}`
  ].filter(Boolean).join(' ');

  return (
    <div className={cardClasses} {...props}>
      {Icon && (
        <div className="metric-card__icon">
          <Icon size={32} />
        </div>
      )}
      <div className="metric-card__value">{value}</div>
      <div className="metric-card__label">{label}</div>
      {change && (
        <div className={changeClasses}>
          {changeType === 'positive' && '↑ '}
          {changeType === 'negative' && '↓ '}
          {change}
        </div>
      )}
    </div>
  );
};

export default MetricCard;

// Made with Bob
