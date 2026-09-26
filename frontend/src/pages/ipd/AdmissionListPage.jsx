import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ipdService from '../../services/ipdService';

const AdmissionListPage = () => {
  const [admissions, setAdmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    fetchAdmissions();
  }, [statusFilter]);

  const fetchAdmissions = async () => {
    setLoading(true);
    try {
      const filters = {};
      if (statusFilter) filters.status = statusFilter;
      const res = await ipdService.getAllAdmissions(filters);
      setAdmissions(res.data?.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load IPD admissions');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'REQUESTED': return 'bg-warning text-dark';
      case 'APPROVED': return 'bg-info text-dark';
      case 'ADMITTED': return 'bg-success';
      case 'ON_LEAVE': return 'bg-secondary';
      case 'DISCHARGE_PLANNED': return 'bg-primary';
      case 'DISCHARGED': return 'bg-dark';
      case 'CANCELLED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">IPD Admissions Registry</h2>
          <p className="text-muted small">Manage patient admissions, bed assignments, transfers, and discharges</p>
        </div>
        <Link to="/ipd/admissions/new" className="btn btn-primary shadow-sm">
          <i className="bi bi-plus-lg me-1"></i> New Admission Request
        </Link>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm p-3 mb-4 rounded-3">
        <div className="row align-items-center">
          <div className="col-md-6">
            <select
              className="form-select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Admission Statuses</option>
              <option value="REQUESTED">REQUESTED</option>
              <option value="APPROVED">APPROVED</option>
              <option value="ADMITTED">ADMITTED</option>
              <option value="DISCHARGE_PLANNED">DISCHARGE_PLANNED</option>
              <option value="DISCHARGED">DISCHARGED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>
      ) : (
        <div className="card border-0 shadow-sm rounded-3">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr>
                  <th>Admission ID</th>
                  <th>Patient</th>
                  <th>Doctor</th>
                  <th>Department</th>
                  <th>Ward & Bed</th>
                  <th>Admission Type</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th className="text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                {admissions.map(adm => (
                  <tr key={adm.id}>
                    <td>
                      <span className="fw-bold font-monospace text-primary">{adm.admissionId}</span>
                    </td>
                    <td>
                      <div className="fw-semibold text-dark">{adm.patientName}</div>
                      <div className="small text-muted font-monospace">{adm.patientCode}</div>
                    </td>
                    <td>Dr. {adm.doctorName}</td>
                    <td>{adm.departmentName}</td>
                    <td>
                      {adm.wardName ? (
                        <div>
                          <span className="badge bg-light text-dark border me-1">{adm.wardCode}</span>
                          <span className="fw-semibold small">{adm.bedCode || 'No Bed'}</span>
                        </div>
                      ) : (
                        <span className="text-muted small">Not Assigned</span>
                      )}
                    </td>
                    <td><span className="badge bg-secondary-subtle text-dark">{adm.admissionType}</span></td>
                    <td>{adm.admissionDate}</td>
                    <td>
                      <span className={`badge ${getStatusBadge(adm.status)}`}>{adm.status}</span>
                    </td>
                    <td className="text-end">
                      <Link to={`/ipd/admissions/${adm.id}`} className="btn btn-sm btn-outline-primary me-2">
                        <i className="bi bi-eye"></i> View
                      </Link>
                    </td>
                  </tr>
                ))}
                {admissions.length === 0 && (
                  <tr>
                    <td colSpan="9" className="text-center text-muted py-4">No IPD admissions found.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdmissionListPage;
