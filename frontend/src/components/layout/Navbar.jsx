import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import {
  Header,
  HeaderName,
  HeaderGlobalBar,
  HeaderGlobalAction,
  Search,
} from '@carbon/react';
import {
  Menu,
  Notification,
  UserAvatar,
  Search as SearchIcon,
} from '@carbon/icons-react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_ENDPOINTS } from '../../config/api';
import './Navbar.scss';

/**
 * Navbar - Fixed top navigation bar
 * 
 * Features:
 * - Fixed position at top
 * - Platform logo and branding
 * - Search bar (hidden on mobile)
 * - Notifications with badge
 * - User profile dropdown
 * - Hamburger menu for mobile
 * - Glassmorphism background
 */
const Navbar = ({ onMenuToggle, isMobile }) => {
  const navigate = useNavigate();
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchValue, setSearchValue] = useState('');
  const [notificationCount, setNotificationCount] = useState(0);

  useEffect(() => {
    fetchNotificationCount();
    const interval = setInterval(fetchNotificationCount, 30000); // Update every 30 seconds
    return () => clearInterval(interval);
  }, []);

  const fetchNotificationCount = async () => {
    try {
      // Count high-risk transactions (fraud score >= 0.5 or FLAGGED/REVIEW status)
      const response = await axios.get(API_ENDPOINTS.TRANSACTIONS);
      const transactions = response.data;
      const highRiskCount = transactions.filter(
        t => t.fraudScore >= 0.5 || t.status === 'FLAGGED' || t.status === 'REVIEW'
      ).length;
      setNotificationCount(highRiskCount);
    } catch (error) {
      console.error('Error fetching notification count:', error);
      setNotificationCount(0);
    }
  };

  const handleSearchToggle = () => {
    setSearchOpen(!searchOpen);
  };

  const handleSearchChange = (e) => {
    setSearchValue(e.target.value);
  };

  const handleSearchSubmit = (e) => {
    if (e.key === 'Enter' && searchValue.trim()) {
      console.log('Searching for:', searchValue);
      // TODO: Implement actual search functionality
      // For now, just log the search term
      alert(`Searching for: ${searchValue}`);
    }
  };

  const handleSearchClear = () => {
    setSearchValue('');
  };

  const handleSearchContainerClick = (e) => {
    // Focus the input when clicking anywhere in the search container
    const input = e.currentTarget.querySelector('input');
    if (input && e.target !== input) {
      input.focus();
    }
  };

  return (
    <Header aria-label="Fraud Investigation Platform" className="navbar">
      <div className="navbar__container">
        {/* Menu Toggle Button */}
        <button
          className="navbar__menu-button"
          onClick={onMenuToggle}
          aria-label="Toggle menu"
          type="button"
        >
          <Menu size={24} />
        </button>

        {/* Platform Logo & Name */}
        <HeaderName href="/" prefix="">
          <div className="navbar__brand">
            <div className="navbar__logo">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect width="32" height="32" rx="8" fill="url(#logo-gradient)" />
                <path
                  d="M16 8L22 12V20L16 24L10 20V12L16 8Z"
                  stroke="white"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
                <circle cx="16" cy="16" r="3" fill="white" />
                <defs>
                  <linearGradient id="logo-gradient" x1="0" y1="0" x2="32" y2="32">
                    <stop offset="0%" stopColor="#6366f1" />
                    <stop offset="100%" stopColor="#a855f7" />
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <span className="navbar__title">
              Fraud Investigation
            </span>
          </div>
        </HeaderName>

        {/* Search Bar - Desktop/Tablet Only */}
        {!isMobile && (
          <div className="navbar__search" onClick={handleSearchContainerClick}>
            <Search
              size="lg"
              placeholder="Search transactions, alerts..."
              labelText="Search"
              closeButtonLabelText="Clear search"
              id="navbar-search"
              value={searchValue}
              onChange={handleSearchChange}
              onKeyDown={handleSearchSubmit}
              onClear={handleSearchClear}
            />
          </div>
        )}

        {/* Global Actions */}
        <HeaderGlobalBar className="navbar__actions">
          {/* Mobile Search Toggle */}
          {isMobile && (
            <HeaderGlobalAction
              aria-label="Search"
              onClick={handleSearchToggle}
              className="navbar__action"
            >
              <SearchIcon size={20} />
            </HeaderGlobalAction>
          )}

          {/* Notifications */}
          <HeaderGlobalAction
            aria-label=""
            tooltipAlignment="end"
            className="navbar__action navbar__action--notification"
            onClick={() => navigate('/alerts')}
          >
            <Notification size={20} />
            {notificationCount > 0 && (
              <span className="navbar__badge">{notificationCount}</span>
            )}
          </HeaderGlobalAction>

          {/* User Profile */}
          <HeaderGlobalAction
            aria-label=""
            tooltipAlignment="end"
            className="navbar__action navbar__action--profile"
          >
            <UserAvatar size={20} />
          </HeaderGlobalAction>
        </HeaderGlobalBar>
      </div>

      {/* Mobile Search Overlay */}
      {isMobile && searchOpen && (
        <div className="navbar__search-mobile">
          <Search
            size="lg"
            placeholder="Search transactions, alerts..."
            labelText="Search"
            closeButtonLabelText="Clear search"
            id="navbar-search-mobile"
            value={searchValue}
            onChange={handleSearchChange}
            onKeyDown={handleSearchSubmit}
            onClear={handleSearchClear}
            onBlur={() => setSearchOpen(false)}
            autoFocus
          />
        </div>
      )}
    </Header>
  );
};

Navbar.propTypes = {
  onMenuToggle: PropTypes.func.isRequired,
  isMobile: PropTypes.bool,
};

Navbar.defaultProps = {
  isMobile: false,
};

export default Navbar;

// Made with Bob