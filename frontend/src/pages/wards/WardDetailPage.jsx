import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import wardService from '../../services/wardService';
import bedService from '../../services/bedService';

const WardDetailPage = () => {
  const { id } = useParams();
  const [ward, setWard] = useState(null);
  const [beds, setBeds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchWardAndBeds();
  }, [id]);

  const fetchWardAndBeds = async () => {
    setLoading(true);
    try {
      const [wRes, bRes] = await Promise.all([
        wardService.getWardById(id),
        bedService.getBedsByWard(id)
      ]);
      setWard(wRes.data);
      setBeds(bRes.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load ward details');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'AVAILABLE': return 'bg-success';
      case 'OCCUPIED': return 'bg-primary';
      case 'CLEANING': return 'bg-warning text-dark';
      case 'RESERVED': return 'bg-info text-dark';
      case 'MAINTENANCE': return 'bg-danger';
      case 'OUT_OF_SERVICE': return 'bg-secondary';
      default: return 'bg-secondary';
    }
  };

  if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>;
  if (error) return <div className="alert alert-danger m-4">{error}</div>;
  if (!ward) return <div className="alert alert-warning m-4">Ward not found</div>;

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <span className="badge bg-primary font-monospace mb-1">{ward.wardCode}</span>
          <h2 className="h3 fw-bold text-dark">{ward.wardName}</h2>
          <p className="text-muted small mb-0">Department: {ward.departmentName} | {ward.building}, {ward.floor}</p>
        </div>
        <div>
          <Link to={`/wards/${id}/edit`} className="btn btn-outline-primary me-2">
            <i className="bi bi-pencil me-1"></i> Edit Ward
          </Link>
          <Link to="/wards" className="btn btn-outline-secondary">
            <i className="bi bi-arrow-left me-1"></i> Back to Wards
          </Link>
        </div>
      </div>

      <div className="row g-4 mb-4">
        <div className="col-md-3">
          <div className="card border-0 shadow-sm p-3 rounded-3 text-center">
            <div className="text-muted small fw-semibold text-uppercase">Capacity</div>
            <div className="h2 fw-bold text-dark mb-0">{ward.capacity}</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card border-0 shadow-sm p-3 rounded-3 text-center">
            <div className="text-muted small fw-semibold text-uppercase">Occupied Beds</div>
            <div className="h2 fw-bold text-primary mb-0">{ward.occupiedBeds}</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card border-0 shadow-sm p-3 rounded-3 text-center">
            <div className="text-muted small fw-semibold text-uppercase">Available Beds</div>
            <div className="h2 fw-bold text-success mb-0">{ward.availableBeds}</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card border-0 shadow-sm p-3 rounded-3 text-center">
            <div className="text-muted small fw-semibold text-uppercase">Gender Policy</div>
            <div className="h2 fw-bold text-dark mb-0">{ward.genderPolicy}</div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white border-0 py-3 px-4 d-flex justify-content-between align-items-center">
          <h5 className="fw-bold mb-0">Ward Beds ({beds.length})</h5>
          <Link to="/beds" className="btn btn-sm btn-outline-primary">
            <i className="bi bi-plus-lg me-1"></i> Manage Beds
          </Link>
        </div>
        <div className="card-body p-4">
          <div className="row g-3">
            {beds.map(b => (
              <div key={b.id} className="col-sm-6 col-md-4 col-lg-3">
                <div className="card h-100 border p-3 rounded-3 hover-shadow transition">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="fw-bold text-dark font-monospace">{b.bedCode}</span>
                    <span className={`badge ${getStatusBadge(b.status)}`}>{b.status}</span>
                  </div>
                  <div className="small text-muted mb-2">Number: Bed-{b.bedNumber} &bull; {b.bedType}</div>
                  {b.status === 'OCCUPIED' && b.currentPatientName && (
                    <div className="bg-light p-2 rounded small border">
                      <div className="fw-semibold text-primary">{b.currentPatientName}</div>
                      <div className="text-muted small font-monospace">{b.currentAdmissionId}</div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default WardDetailPage;
