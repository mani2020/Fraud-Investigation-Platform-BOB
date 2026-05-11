import React from 'react';
import PropTypes from 'prop-types';
import { Breadcrumb, BreadcrumbItem } from '@carbon/react';
import './PageHeader.scss';

/**
 * PageHeader - Reusable page header component
 * 
 * Features:
 * - Page title with optional icon
 * - Breadcrumb navigation
 * - Action buttons area
 * - Responsive layout
 * - Subtitle support
 */
const PageHeader = ({
  title,
  subtitle,
  icon: Icon,
  breadcrumbs,
  actions,
  className,
}) => {
  return (
    <div className={`page-header ${className || ''}`}>
      {/* Breadcrumbs */}
      {breadcrumbs && breadcrumbs.length > 0 && (
        <Breadcrumb noTrailingSlash className="page-header__breadcrumbs">
          {breadcrumbs.map((crumb, index) => (
            <BreadcrumbItem
              key={index}
              href={crumb.href}
              isCurrentPage={index === breadcrumbs.length - 1}
            >
              {crumb.label}
            </BreadcrumbItem>
          ))}
        </Breadcrumb>
      )}

      {/* Header Content */}
      <div className="page-header__content">
        <div className="page-header__title-section">
          {Icon && (
            <div className="page-header__icon">
              <Icon size={32} />
            </div>
          )}
          <div className="page-header__text">
            <h1 className="page-header__title">{title}</h1>
            {subtitle && <p className="page-header__subtitle">{subtitle}</p>}
          </div>
        </div>

        {/* Actions */}
        {actions && <div className="page-header__actions">{actions}</div>}
      </div>
    </div>
  );
};

PageHeader.propTypes = {
  title: PropTypes.string.isRequired,
  subtitle: PropTypes.string,
  icon: PropTypes.elementType,
  breadcrumbs: PropTypes.arrayOf(
    PropTypes.shape({
      label: PropTypes.string.isRequired,
      href: PropTypes.string,
    })
  ),
  actions: PropTypes.node,
  className: PropTypes.string,
};

PageHeader.defaultProps = {
  subtitle: null,
  icon: null,
  breadcrumbs: null,
  actions: null,
  className: '',
};

export default PageHeader;

// Made with Bob