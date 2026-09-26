import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import wardService from '../../services/wardService';

const WardListPage = () => {
  const [wards, setWards] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchWards();
  }, []);

  const fetchWards = async () => {
    setLoading(true);
    try {
      const res = await wardService.getAllWards();
      setWards(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load wards');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (id, currentStatus) => {
    try {
      await wardService.updateWardStatus(id, !currentStatus);
      fetchWards();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update ward status');
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 font-weight-bold text-gray-800 mb-1">Ward Management</h2>
          <p className="text-muted small">Manage hospital wards, capacity, and department mappings</p>
        </div>
        <Link to="/wards/new" className="btn btn-primary shadow-sm">
          <i className="bi bi-plus-lg me-1"></i> Add New Ward
        </Link>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading Wards...</span>
          </div>
        </div>
      ) : (
        <div className="row g-4">
          {wards.map((w) => (
            <div key={w.id} className="col-md-6 col-lg-4">
              <div className="card h-100 border-0 shadow-sm rounded-3 hover-shadow transition">
                <div className="card-header bg-white border-0 pt-3 px-4 d-flex justify-content-between align-items-center">
                  <span className="badge bg-primary-subtle text-primary font-monospace px-2.5 py-1">
                    {w.wardCode}
                  </span>
                  <span className={`badge ${w.isActive ? 'bg-success' : 'bg-secondary'}`}>
                    {w.isActive ? 'ACTIVE' : 'INACTIVE'}
                  </span>
                </div>
                <div className="card-body px-4 py-3">
                  <h5 className="card-title fw-bold text-dark mb-1">{w.wardName}</h5>
                  <p className="text-muted small mb-3">
                    <i className="bi bi-building me-1"></i> {w.departmentName} &bull; {w.floor || '1st Floor'} ({w.building || 'Main Building'})
                  </p>
                  
                  <div className="d-flex justify-content-between align-items-center bg-light p-2.5 rounded-2 mb-3">
                    <div className="text-center px-2">
                      <span className="d-block small text-muted">Type</span>
                      <span className="fw-semibold small">{w.wardType}</span>
                    </div>
                    <div className="text-center border-start border-end px-3">
                      <span className="d-block small text-muted">Capacity</span>
                      <span className="fw-bold text-dark">{w.capacity}</span>
                    </div>
                    <div className="text-center px-2">
                      <span className="d-block small text-muted">Available</span>
                      <span className="fw-bold text-success">{w.availableBeds}</span>
                    </div>
                  </div>

                  <div className="progress mb-2" style={{ height: '6px' }}>
                    <div
                      className="progress-bar bg-primary"
                      role="progressbar"
                      style={{
                        width: `${w.capacity > 0 ? (w.occupiedBeds / w.capacity) * 100 : 0}%`
                      }}
                    ></div>
                  </div>
                  <div className="d-flex justify-content-between small text-muted mb-2">
                    <span>Occupied: {w.occupiedBeds}</span>
                    <span>Gender Policy: {w.genderPolicy}</span>
                  </div>
                </div>

                <div className="card-footer bg-light border-0 px-4 py-3 d-flex justify-content-between align-items-center">
                  <Link to={`/wards/${w.id}`} className="btn btn-sm btn-outline-primary">
                    <i className="bi bi-eye me-1"></i> View Details
                  </Link>
                  <div>
                    <Link to={`/wards/${w.id}/edit`} className="btn btn-sm btn-light text-dark me-2">
                      <i className="bi bi-pencil"></i>
                    </Link>
                    <button
                      onClick={() => handleToggleStatus(w.id, w.isActive)}
                      className={`btn btn-sm ${w.isActive ? 'btn-outline-danger' : 'btn-outline-success'}`}
                    >
                      {w.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default WardListPage;
