import React from 'react';

const SelectInput = ({
  label,
  name,
  value,
  onChange,
  options = [],
  error,
  required = false,
  disabled = false
}) => {
  return (
    <div className="mb-3">
      {label && (
        <label htmlFor={name} className="form-label fw-semibold small">
          {label} {required && <span className="text-danger">*</span>}
        </label>
      )}
      <select
        id={name}
        name={name}
        className={`form-select ${error ? 'is-invalid' : ''}`}
        value={value}
        onChange={onChange}
        disabled={disabled}
        required={required}
      >
        <option value="">-- Select {label || 'Option'} --</option>
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      {error && <div className="invalid-feedback d-block">{error}</div>}
    </div>
  );
};

export default SelectInput;
