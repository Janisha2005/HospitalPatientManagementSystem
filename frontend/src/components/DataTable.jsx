import React from 'react';
import LoadingSpinner from './LoadingSpinner';

const DataTable = ({ columns, data, loading = false, emptyMessage = 'No data available' }) => {
  if (loading) {
    return <LoadingSpinner />;
  }

  if (!data || data.length === 0) {
    return (
      <div className="text-center py-5 text-muted">
        <i className="bi bi-inbox fs-1 d-block mb-2 text-secondary opacity-50"></i>
        <span>{emptyMessage}</span>
      </div>
    );
  }

  return (
    <div className="table-responsive">
      <table className="table table-hover align-middle mb-0">
        <thead className="table-light">
          <tr>
            {columns.map((col, idx) => (
              <th key={idx} className={col.className || ''} style={col.style}>
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, rowIdx) => (
            <tr key={row.id || rowIdx}>
              {columns.map((col, colIdx) => (
                <td key={colIdx} className={col.className || ''}>
                  {col.cell ? col.cell(row) : row[col.accessor]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DataTable;
