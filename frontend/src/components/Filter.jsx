import React from 'react';

const Filter = ({ label, options, value, onChange }) => {
  return (
    <div className="d-flex align-items-center">
      {label && <label className="form-label me-2 mb-0 small text-muted text-nowrap">{label}:</label>}
      <select
        className="form-select form-select-sm"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
};

export default Filter;
