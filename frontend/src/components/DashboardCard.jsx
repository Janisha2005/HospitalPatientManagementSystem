import React from 'react';
import { formatIndianNumber } from '../utils/indiaUtils';

const DashboardCard = ({ title, value, icon, color = 'primary' }) => {
  return (
    <div className="card border-0 shadow-sm rounded-3 h-100">
      <div className="card-body p-4 d-flex align-items-center justify-content-between">
        <div>
          <p className="text-uppercase text-muted fw-bold mb-1" style={{ fontSize: '0.75rem', letterSpacing: '0.5px' }}>
            {title}
          </p>
          <h2 className={`display-6 fw-bold mb-0 text-${color}`}>
            {value != null ? formatIndianNumber(value) : '-'}
          </h2>
        </div>
        <div
          className={`rounded-circle bg-${color} bg-opacity-10 p-3 d-flex align-items-center justify-content-center`}
          style={{ width: '56px', height: '56px' }}
        >
          <i className={`bi bi-${icon} fs-3 text-${color}`}></i>
        </div>
      </div>
    </div>
  );
};

export default DashboardCard;
