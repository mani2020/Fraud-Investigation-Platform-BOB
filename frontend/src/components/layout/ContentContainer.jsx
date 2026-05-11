import React from 'react';
import PropTypes from 'prop-types';
import './ContentContainer.scss';

/**
 * ContentContainer - Responsive content wrapper
 * 
 * Features:
 * - Max-width container for content
 * - Responsive padding
 * - Proper spacing
 * - Optional full-width mode
 * - Optional background variants
 */
const ContentContainer = ({
  children,
  maxWidth,
  padding,
  fullWidth,
  background,
  className,
}) => {
  const containerClasses = [
    'content-container',
    fullWidth && 'content-container--full-width',
    background && `content-container--${background}`,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  const style = {
    ...(maxWidth && !fullWidth && { maxWidth }),
    ...(padding && { padding }),
  };

  return (
    <div className={containerClasses} style={style}>
      {children}
    </div>
  );
};

ContentContainer.propTypes = {
  children: PropTypes.node.isRequired,
  maxWidth: PropTypes.string,
  padding: PropTypes.string,
  fullWidth: PropTypes.bool,
  background: PropTypes.oneOf(['transparent', 'glass', 'solid']),
  className: PropTypes.string,
};

ContentContainer.defaultProps = {
  maxWidth: null,
  padding: null,
  fullWidth: false,
  background: 'transparent',
  className: '',
};

export default ContentContainer;

// Made with Bob