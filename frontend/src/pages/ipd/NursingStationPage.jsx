import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import nursingService from '../../services/nursingService';

const NursingStationPage = () => {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    setLoading(true);
    try {
      const res = await nursingService.getNursingDashboard();
      setDashboard(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load nursing workbench');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>;
  if (error) return <div className="alert alert-danger m-4">{error}</div>;

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Nursing Station Workbench</h2>
          <p className="text-muted small">Real-time inpatient care, vital sign monitoring, and nursing shift handovers</p>
        </div>
      </div>

      {/* Metric Cards */}
      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <div className="card border-0 shadow-sm p-3 rounded-3 bg-primary text-white">
            <div className="text-white-50 small fw-bold text-uppercase">Current Inpatients</div>
            <div className="display-6 fw-bold">{dashboard?.currentInpatientsCount || 0}</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card border-0 shadow-sm p-3 rounded-3 bg-success text-white">
            <div className="text-white-50 small fw-bold text-uppercase">New Admissions Today</div>
            <div className="display-6 fw-bold">{dashboard?.newAdmissionsToday || 0}</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card border-0 shadow-sm p-3 rounded-3 bg-warning text-dark">
            <div className="text-black-50 small fw-bold text-uppercase">Discharge Planned</div>
            <div className="display-6 fw-bold">{dashboard?.dischargePlannedCount || 0}</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card border-0 shadow-sm p-3 rounded-3 bg-info text-dark">
            <div className="text-black-50 small fw-bold text-uppercase">Available Beds</div>
            <div className="display-6 fw-bold">{dashboard?.availableBedsCount || 0}</div>
          </div>
        </div>
      </div>

      {/* Active Inpatients Workbench Table */}
      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white border-0 py-3 px-4">
          <h5 className="fw-bold mb-0">Active Inpatient Ward Roster</h5>
        </div>
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light">
              <tr>
                <th>Patient & Admission</th>
                <th>Ward & Bed</th>
                <th>Doctor</th>
                <th>Admission Date</th>
                <th>Status</th>
                <th className="text-end">Nursing Actions</th>
              </tr>
            </thead>
            <tbody>
              {dashboard?.activeInpatients?.map(p => (
                <tr key={p.id}>
                  <td>
                    <div className="fw-bold text-dark">{p.patientName}</div>
                    <div className="small font-monospace text-primary">{p.admissionId}</div>
                  </td>
                  <td>
                    <span className="badge bg-light text-dark border me-1">{p.wardCode}</span>
                    <span className="fw-bold text-primary font-monospace">{p.bedCode}</span>
                  </td>
                  <td>Dr. {p.doctorName}</td>
                  <td className="small">{p.admissionDate}</td>
                  <td>
                    <span className={`badge ${p.status === 'DISCHARGE_PLANNED' ? 'bg-warning text-dark' : 'bg-success'}`}>
                      {p.status}
                    </span>
                  </td>
                  <td className="text-end">
                    <Link to={`/ipd/admissions/${p.id}`} className="btn btn-sm btn-outline-primary me-2">
                      <i className="bi bi-eye"></i> View Chart
                    </Link>
                  </td>
                </tr>
              ))}
              {(!dashboard?.activeInpatients || dashboard.activeInpatients.length === 0) && (
                <tr>
                  <td colSpan="6" className="text-center text-muted py-4">No active inpatients assigned.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default NursingStationPage;
