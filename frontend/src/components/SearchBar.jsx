import React from 'react';

const SearchBar = ({ value, onChange, onSearch, placeholder = 'Search...' }) => {
  const handleSubmit = (e) => {
    e.preventDefault();
    if (onSearch) onSearch();
  };

  return (
    <form onSubmit={handleSubmit} className="d-flex">
      <div className="input-group input-group-sm">
        <span className="input-group-text bg-white">
          <i className="bi bi-search text-muted"></i>
        </span>
        <input
          type="text"
          className="form-control"
          placeholder={placeholder}
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
        {onSearch && (
          <button type="submit" className="btn btn-outline-secondary">
            Search
          </button>
        )}
      </div>
    </form>
  );
};

export default SearchBar;
