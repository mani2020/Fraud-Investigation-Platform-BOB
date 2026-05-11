import React from 'react';
import './PremiumCard.scss';

/**
 * Premium glassmorphism card component with gradient borders and glow effects
 */
const PremiumCard = ({ 
  children, 
  title, 
  subtitle,
  variant = 'default', // default, gradient, glow-purple, glow-cyan, glow-danger
  className = '',
  onClick,
  hoverable = true,
  ...props 
}) => {
  const cardClasses = [
    'premium-card',
    `premium-card--${variant}`,
    hoverable && 'premium-card--hoverable',
    onClick && 'premium-card--clickable',
    className
  ].filter(Boolean).join(' ');

  return (
    <div className={cardClasses} onClick={onClick} {...props}>
      {(title || subtitle) && (
        <div className="premium-card__header">
          {title && <h3 className="premium-card__title">{title}</h3>}
          {subtitle && <p className="premium-card__subtitle">{subtitle}</p>}
        </div>
      )}
      <div className="premium-card__content">
        {children}
      </div>
    </div>
  );
};

export default PremiumCard;

// Made with Bob
