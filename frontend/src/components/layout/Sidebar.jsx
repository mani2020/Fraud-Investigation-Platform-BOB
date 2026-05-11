import React from 'react';
import PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';
import {
  Dashboard,
  Receipt,
  WarningAlt,
  Analytics,
  Search,
} from '@carbon/icons-react';
import './Sidebar.scss';

/**
 * Sidebar - Desktop/Tablet navigation sidebar
 * 
 * Features:
 * - Expanded mode (240px) with icons + labels
 * - Collapsed mode (64px) with icons only
 * - Active state highlighting with gradient
 * - Smooth transitions
 * - Auto-collapse on tablet
 */
const Sidebar = ({ isOpen }) => {
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

  return (
    <aside className={`sidebar ${isOpen ? 'sidebar--open' : 'sidebar--collapsed'}`}>
      <nav className="sidebar__nav" aria-label="Main navigation">
        <ul className="sidebar__list">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <li key={item.path} className="sidebar__item">
                <NavLink
                  to={item.path}
                  className={({ isActive }) =>
                    `sidebar__link ${isActive ? 'sidebar__link--active' : ''}`
                  }
                  title={item.label}
                >
                  <div className="sidebar__icon">
                    <Icon size={24} />
                  </div>
                  <div className="sidebar__content">
                    <span className="sidebar__label">{item.label}</span>
                    <span className="sidebar__description">{item.description}</span>
                  </div>
                  <div className="sidebar__indicator" />
                </NavLink>
              </li>
            );
          })}
        </ul>
      </nav>

      {/* Sidebar Footer */}
      <div className="sidebar__footer">
        <div className="sidebar__status">
          <div className="sidebar__status-indicator" />
          <span className="sidebar__status-text">System Online</span>
        </div>
      </div>
    </aside>
  );
};

Sidebar.propTypes = {
  isOpen: PropTypes.bool,
};

Sidebar.defaultProps = {
  isOpen: true,
};

export default Sidebar;

// Made with Bob