import React, { useState, useEffect } from 'react';
import {
  Header,
  HeaderName,
  HeaderNavigation,
  HeaderMenuItem,
  HeaderGlobalBar,
  HeaderGlobalAction,
  SideNav,
  SideNavItems,
  SideNavLink,
  SkipToContent,
} from '@carbon/react';
import {
  Dashboard,
  Notification,
  Search,
  ChartLine,
  Security,
  User,
  Settings,
} from '@carbon/icons-react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { API_ENDPOINTS } from '../../config/api';
import './DashboardLayout.scss';

const DashboardLayout = ({ children }) => {
  const [isSideNavExpanded, setIsSideNavExpanded] = useState(false);
  const [notificationCount, setNotificationCount] = useState(0);
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

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

  return (
    <div className="premium-dashboard-layout">
      <Header aria-label="Fraud Investigation Platform" className="premium-header">
        <SkipToContent />
        <HeaderName href="#" prefix="" className="premium-header-name">
          <Security size={24} className="header-icon" />
          <span className="header-title">
            <span className="header-title-main">Fraud Investigation</span>
            <span className="header-title-sub">Platform</span>
          </span>
        </HeaderName>
        
        <HeaderNavigation aria-label="Navigation" className="premium-nav">
          <HeaderMenuItem
            onClick={() => navigate('/dashboard')}
            isActive={isActive('/dashboard')}
            className={isActive('/dashboard') ? 'active-nav-item' : ''}
          >
            <Dashboard size={16} className="nav-icon" />
            Dashboard
          </HeaderMenuItem>
          <HeaderMenuItem
            onClick={() => navigate('/alerts')}
            isActive={isActive('/alerts')}
            className={isActive('/alerts') ? 'active-nav-item' : ''}
          >
            <Notification size={16} className="nav-icon" />
            Alerts
          </HeaderMenuItem>
          <HeaderMenuItem
            onClick={() => navigate('/analytics')}
            isActive={isActive('/analytics')}
            className={isActive('/analytics') ? 'active-nav-item' : ''}
          >
            <ChartLine size={16} className="nav-icon" />
            Analytics
          </HeaderMenuItem>
        </HeaderNavigation>
        
        <HeaderGlobalBar className="premium-global-bar">
          <HeaderGlobalAction
            aria-label="Search"
            tooltipAlignment="end"
            className="global-action"
          >
            <Search size={20} />
          </HeaderGlobalAction>
          <HeaderGlobalAction
            aria-label="Notifications"
            tooltipAlignment="end"
            className="global-action notification-action"
            onClick={() => navigate('/alerts')}
          >
            <Notification size={20} />
            {notificationCount > 0 && (
              <span className="notification-badge">{notificationCount}</span>
            )}
          </HeaderGlobalAction>
          <HeaderGlobalAction
            aria-label="Settings"
            tooltipAlignment="end"
            className="global-action"
          >
            <Settings size={20} />
          </HeaderGlobalAction>
          <HeaderGlobalAction
            aria-label="User Profile"
            tooltipAlignment="end"
            className="global-action user-action"
          >
            <User size={20} />
          </HeaderGlobalAction>
        </HeaderGlobalBar>
        
        <SideNav
          aria-label="Side navigation"
          expanded={isSideNavExpanded}
          onOverlayClick={() => setIsSideNavExpanded(false)}
          className="premium-sidenav"
        >
          <SideNavItems>
            <SideNavLink
              renderIcon={Dashboard}
              onClick={() => navigate('/dashboard')}
              isActive={isActive('/dashboard')}
              className={isActive('/dashboard') ? 'active-sidenav-item' : ''}
            >
              Transaction Monitor
            </SideNavLink>
            <SideNavLink
              renderIcon={Notification}
              onClick={() => navigate('/alerts')}
              isActive={isActive('/alerts')}
              className={isActive('/alerts') ? 'active-sidenav-item' : ''}
            >
              Fraud Alerts
            </SideNavLink>
            <SideNavLink
              renderIcon={ChartLine}
              onClick={() => navigate('/analytics')}
              isActive={isActive('/analytics')}
              className={isActive('/analytics') ? 'active-sidenav-item' : ''}
            >
              Analytics
            </SideNavLink>
          </SideNavItems>
        </SideNav>
      </Header>
      
      <div className="premium-content-wrapper">
        <div className="premium-content-container">
          {children}
        </div>
      </div>
      
      {/* Animated background elements */}
      <div className="background-orbs">
        <div className="orb orb-1"></div>
        <div className="orb orb-2"></div>
        <div className="orb orb-3"></div>
      </div>
    </div>
  );
};

export default DashboardLayout;

// Made with Bob