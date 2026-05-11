import React from 'react';
import { Select, SelectItem } from '@carbon/react';
import { ChevronLeft, ChevronRight, PageFirst, PageLast } from '@carbon/icons-react';
import './CustomPagination.scss';

const CustomPagination = ({
  page,
  pageSize,
  totalItems,
  pageSizes = [10, 20, 30, 40, 50],
  onChange
}) => {
  const totalPages = Math.ceil(totalItems / pageSize);
  const startItem = (page - 1) * pageSize + 1;
  const endItem = Math.min(page * pageSize, totalItems);

  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= totalPages) {
      onChange({ page: newPage, pageSize });
    }
  };

  const handlePageSizeChange = (e) => {
    const newPageSize = parseInt(e.target.value);
    onChange({ page: 1, pageSize: newPageSize });
  };

  return (
    <div className="custom-pagination">
      <div className="pagination-info">
        {startItem}–{endItem} of {totalItems}
      </div>

      <div className="pagination-controls">
        <button
          className="pagination-btn"
          disabled={page === 1}
          onClick={() => handlePageChange(1)}
          title="First page"
        >
          <PageFirst size={16} />
        </button>
        
        <button
          className="pagination-btn"
          disabled={page === 1}
          onClick={() => handlePageChange(page - 1)}
          title="Previous page"
        >
          <ChevronLeft size={16} />
        </button>
        
        <div className="page-selector">
          <Select
            id="page-select"
            value={page}
            onChange={(e) => handlePageChange(parseInt(e.target.value))}
            size="sm"
            hideLabel
            labelText="Page"
          >
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
              <SelectItem key={p} value={p} text={`${p}`} />
            ))}
          </Select>
          <span className="page-text">of {totalPages}</span>
        </div>

        <button
          className="pagination-btn"
          disabled={page === totalPages}
          onClick={() => handlePageChange(page + 1)}
          title="Next page"
        >
          <ChevronRight size={16} />
        </button>
        
        <button
          className="pagination-btn"
          disabled={page === totalPages}
          onClick={() => handlePageChange(totalPages)}
          title="Last page"
        >
          <PageLast size={16} />
        </button>
      </div>

      <div className="page-size-selector">
        <Select
          id="page-size-select"
          value={pageSize}
          onChange={handlePageSizeChange}
          size="sm"
          hideLabel
          labelText="Items per page"
        >
          {pageSizes.map((size) => (
            <SelectItem key={size} value={size} text={`${size} per page`} />
          ))}
        </Select>
      </div>
    </div>
  );
};

export default CustomPagination;

// Made with Bob