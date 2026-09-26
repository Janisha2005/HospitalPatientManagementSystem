import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ipdService from '../../services/ipdService';

const IPDDashboardPage = () => {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMetrics();
  }, []);

  const fetchMetrics = async () => {
    setLoading(true);
    try {
      const res = await ipdService.getDashboardMetrics();
      setMetrics(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load IPD dashboard metrics');
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
          <h2 className="h3 fw-bold text-dark mb-1">Inpatient Department (IPD) Dashboard</h2>
          <p className="text-muted small">Live inpatient census, bed occupancy, and discharge monitoring</p>
        </div>
        <div>
          <Link to="/ipd/admissions/new" className="btn btn-primary shadow-sm me-2">
            <i className="bi bi-plus-lg me-1"></i> New Admission Request
          </Link>
          <Link to="/ipd/nursing-station" className="btn btn-outline-primary shadow-sm">
            <i className="bi bi-person-badge me-1"></i> Nursing Station
          </Link>
        </div>
      </div>

      {/* Primary Census Metric Cards */}
      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-primary text-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-white-50 small fw-bold text-uppercase">Total Active Inpatients</div>
                <div className="display-6 fw-bold mb-0">{metrics?.totalActiveAdmissions || 0}</div>
              </div>
              <i className="bi bi-hospital fs-1 text-white-50"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-success text-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-white-50 small fw-bold text-uppercase">Today's Admissions</div>
                <div className="display-6 fw-bold mb-0">{metrics?.todayAdmissions || 0}</div>
              </div>
              <i className="bi bi-box-arrow-in-right fs-1 text-white-50"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-info text-dark">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-black-50 small fw-bold text-uppercase">Available Beds</div>
                <div className="display-6 fw-bold mb-0">{metrics?.availableBeds || 0}</div>
              </div>
              <i className="bi bi-lamp fs-1 text-black-50"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-warning text-dark">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-black-50 small fw-bold text-uppercase">Discharge Planned</div>
                <div className="display-6 fw-bold mb-0">{metrics?.dischargePlanned || 0}</div>
              </div>
              <i className="bi bi-box-arrow-right fs-1 text-black-50"></i>
            </div>
          </div>
        </div>
      </div>

      {/* Bed Status Breakdown */}
      <div className="row g-4 mb-4">
        <div className="col-lg-6">
          <div className="card border-0 shadow-sm rounded-3 h-100">
            <div className="card-header bg-white border-0 py-3 px-4">
              <h5 className="fw-bold mb-0">Bed Census Breakdown</h5>
            </div>
            <div className="card-body p-4">
              <div className="list-group list-group-flush">
                <div className="list-group-item d-flex justify-content-between align-items-center px-0 py-2.5">
                  <span className="fw-semibold text-dark"><i className="bi bi-circle-fill text-primary me-2"></i>Occupied Beds</span>
                  <span className="badge bg-primary fs-6 px-3">{metrics?.occupiedBeds || 0}</span>
                </div>
                <div className="list-group-item d-flex justify-content-between align-items-center px-0 py-2.5">
                  <span className="fw-semibold text-dark"><i className="bi bi-circle-fill text-success me-2"></i>Available Beds</span>
                  <span className="badge bg-success fs-6 px-3">{metrics?.availableBeds || 0}</span>
                </div>
                <div className="list-group-item d-flex justify-content-between align-items-center px-0 py-2.5">
                  <span className="fw-semibold text-dark"><i className="bi bi-circle-fill text-warning me-2"></i>Beds in Sanitization / Cleaning</span>
                  <span className="badge bg-warning text-dark fs-6 px-3">{metrics?.bedsInCleaning || 0}</span>
                </div>
                <div className="list-group-item d-flex justify-content-between align-items-center px-0 py-2.5">
                  <span className="fw-semibold text-dark"><i className="bi bi-circle-fill text-danger me-2"></i>Beds Under Maintenance</span>
                  <span className="badge bg-danger fs-6 px-3">{metrics?.bedsUnderMaintenance || 0}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-lg-6">
          <div className="card border-0 shadow-sm rounded-3 h-100">
            <div className="card-header bg-white border-0 py-3 px-4">
              <h5 className="fw-bold mb-0">Admission & Discharge Workflow</h5>
            </div>
            <div className="card-body p-4">
              <div className="list-group list-group-flush">
                <div className="list-group-item d-flex justify-content-between align-items-center px-0 py-2.5">
                  <span className="fw-semibold text-dark"><i className="bi bi-hourglass-split me-2 text-warning"></i>Pending Admission Requests</span>
                  <span className="badge bg-warning text-dark fs-6 px-3">{metrics?.pendingAdmissionRequests || 0}</span>
                </div>
                <div className="list-group-item d-flex justify-content-between align-items-center px-0 py-2.5">
                  <span className="fw-semibold text-dark"><i className="bi bi-box-arrow-in-right me-2 text-success"></i>Admitted Today</span>
                  <span className="badge bg-success fs-6 px-3">{metrics?.todayAdmissions || 0}</span>
                </div>
                <div className="list-group-item d-flex justify-content-between align-items-center px-0 py-2.5">
                  <span className="fw-semibold text-dark"><i className="bi bi-box-arrow-right me-2 text-info"></i>Discharged Today</span>
                  <span className="badge bg-info text-dark fs-6 px-3">{metrics?.todayDischarges || 0}</span>
                </div>
              </div>

              <div className="mt-4 pt-3 border-top d-flex gap-2">
                <Link to="/ipd/admissions" className="btn btn-outline-primary flex-fill">
                  View All Admissions
                </Link>
                <Link to="/beds" className="btn btn-outline-secondary flex-fill">
                  View Bed Map
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default IPDDashboardPage;
