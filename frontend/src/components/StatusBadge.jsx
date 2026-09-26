import React from 'react';

const StatusBadge = ({ isActive, activeText = 'ACTIVE', inactiveText = 'INACTIVE' }) => {
  return (
    <span className={`badge ${isActive ? 'bg-success' : 'bg-secondary'}`}>
      {isActive ? activeText : inactiveText}
    </span>
  );
};

export default StatusBadge;
