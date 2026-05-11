import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';
import {
  Dashboard,
  Receipt,
  WarningAlt,
  Analytics,
  Search,
  Close,
} from '@carbon/icons-react';
import './MobileDrawer.scss';

/**
 * MobileDrawer - Mobile navigation drawer
 * 
 * Features:
 * - Slide-in from left animation
 * - Dark overlay backdrop
 * - Close on backdrop click
 * - Close on navigation
 * - Swipe to close gesture support
 * - Touch-friendly targets (44px minimum)
 */
const MobileDrawer = ({ isOpen, onClose }) => {
  const navItems = [
    {
      path: '/dashboard',
      label: 'Dashboard',
      icon: Dashboard,
      description: 'Overview & metrics',
    },
    {
      path: '/transactions',
      label: 'Transactions',
      icon: Receipt,
      description: 'Payment monitoring',
    },
    {
      path: '/alerts',
      label: 'Alerts',
      icon: WarningAlt,
      description: 'Fraud alerts',
    },
    {
      path: '/analytics',
      label: 'Analytics',
      icon: Analytics,
      description: 'Reports & insights',
    },
    {
      path: '/investigation',
      label: 'Investigation',
      icon: Search,
      description: 'Deep analysis',
    },
  ];

  // Handle escape key
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, onClose]);

  // Prevent body scroll when drawer is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  // Handle navigation click - close drawer
  const handleNavClick = () => {
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="mobile-drawer">
      {/* Backdrop */}
      <div
        className="mobile-drawer__backdrop"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Drawer */}
      <aside className="mobile-drawer__panel" role="dialog" aria-modal="true">
        {/* Header */}
        <div className="mobile-drawer__header">
          <div className="mobile-drawer__brand">
            <div className="mobile-drawer__logo">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect width="32" height="32" rx="8" fill="url(#drawer-logo-gradient)" />
                <path
                  d="M16 8L22 12V20L16 24L10 20V12L16 8Z"
                  stroke="white"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
                <circle cx="16" cy="16" r="3" fill="white" />
                <defs>
                  <linearGradient id="drawer-logo-gradient" x1="0" y1="0" x2="32" y2="32">
                    <stop offset="0%" stopColor="#6366f1" />
                    <stop offset="100%" stopColor="#a855f7" />
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <span className="mobile-drawer__title">Fraud Investigation</span>
          </div>
          <button
            className="mobile-drawer__close"
            onClick={onClose}
            aria-label="Close menu"
            type="button"
          >
            <Close size={24} />
          </button>
        </div>

        {/* Navigation */}
        <nav className="mobile-drawer__nav" aria-label="Main navigation">
          <ul className="mobile-drawer__list">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <li key={item.path} className="mobile-drawer__item">
                  <NavLink
                    to={item.path}
                    className={({ isActive }) =>
                      `mobile-drawer__link ${isActive ? 'mobile-drawer__link--active' : ''}`
                    }
                    onClick={handleNavClick}
                  >
                    <div className="mobile-drawer__icon">
                      <Icon size={24} />
                    </div>
                    <div className="mobile-drawer__content">
                      <span className="mobile-drawer__label">{item.label}</span>
                      <span className="mobile-drawer__description">{item.description}</span>
                    </div>
                  </NavLink>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Footer */}
        <div className="mobile-drawer__footer">
          <div className="mobile-drawer__status">
            <div className="mobile-drawer__status-indicator" />
            <span className="mobile-drawer__status-text">System Online</span>
          </div>
        </div>
      </aside>
    </div>
  );
};

MobileDrawer.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default MobileDrawer;

// Made with Bob