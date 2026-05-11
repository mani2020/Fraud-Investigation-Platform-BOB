import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';
import MobileDrawer from '../components/layout/MobileDrawer';
import './AppLayout.scss';

/**
 * AppLayout - Main application layout component
 * Provides responsive layout with navbar, sidebar, and content area
 * 
 * Features:
 * - Fixed top navbar (64px height)
 * - Collapsible sidebar (240px expanded, 64px collapsed)
 * - Responsive mobile drawer
 * - Proper z-index management
 * - Smooth transitions
 */
const AppLayout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(false);

  // Handle responsive behavior
  useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth < 768;
      setIsMobile(mobile);
      
      // Auto-collapse sidebar on tablet
      if (window.innerWidth >= 768 && window.innerWidth < 1024) {
        setSidebarOpen(false);
      } else if (window.innerWidth >= 1024) {
        setSidebarOpen(true);
      }
    };

    // Initial check
    handleResize();

    // Add resize listener
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Toggle sidebar (desktop/tablet)
  const toggleSidebar = () => {
    if (isMobile) {
      setMobileDrawerOpen(!mobileDrawerOpen);
    } else {
      setSidebarOpen(!sidebarOpen);
    }
  };

  // Close mobile drawer
  const closeMobileDrawer = () => {
    setMobileDrawerOpen(false);
  };

  return (
    <div className={`app-layout ${sidebarOpen ? 'sidebar-open' : 'sidebar-collapsed'}`}>
      {/* Fixed Navbar */}
      <Navbar onMenuToggle={toggleSidebar} isMobile={isMobile} />

      {/* Desktop/Tablet Sidebar */}
      {!isMobile && (
        <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      )}

      {/* Mobile Drawer */}
      {isMobile && (
        <MobileDrawer isOpen={mobileDrawerOpen} onClose={closeMobileDrawer} />
      )}

      {/* Main Content Area */}
      <main className="app-layout__content">
        <div className="app-layout__content-inner">
          {children}
        </div>
      </main>
    </div>
  );
};

AppLayout.propTypes = {
  children: PropTypes.node.isRequired,
};

export default AppLayout;

// Made with Bob