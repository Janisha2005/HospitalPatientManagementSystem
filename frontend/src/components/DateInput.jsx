import React from 'react';

const DateInput = ({
  label,
  name,
  value,
  onChange,
  error,
  required = false,
  max,
  min,
  disabled = false
}) => {
  return (
    <div className="mb-3">
      {label && (
        <label htmlFor={name} className="form-label fw-semibold small">
          {label} {required && <span className="text-danger">*</span>}
        </label>
      )}
      <input
        id={name}
        name={name}
        type="date"
        className={`form-control ${error ? 'is-invalid' : ''}`}
        value={value}
        onChange={onChange}
        max={max}
        min={min}
        disabled={disabled}
        required={required}
      />
      {error && <div className="invalid-feedback d-block">{error}</div>}
    </div>
  );
};

export default DateInput;
