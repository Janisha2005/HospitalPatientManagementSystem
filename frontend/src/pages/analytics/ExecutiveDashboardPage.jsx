import React, { useState, useEffect } from 'react';
import { analyticsService } from '../../services/analyticsService';

const ExecutiveDashboardPage = () => {
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState('TODAY');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [analytics, setAnalytics] = useState(null);
  const [error, setError] = useState(null);

  const fetchAnalytics = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = { period };
      if (period === 'CUSTOM') {
        params.fromDate = fromDate;
        params.toDate = toDate;
      }
      const response = await analyticsService.getExecutiveAnalytics(params);
      if (response.success) {
        setAnalytics(response.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to load executive analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalytics();
  }, [period]);

  const handleCustomFilterSubmit = (e) => {
    e.preventDefault();
    if (period === 'CUSTOM') {
      fetchAnalytics();
    }
  };

  const formatINR = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    }).format(amount || 0);
  };

  return (
    <div className="container-fluid py-3">
      {/* Header & Filter Controls */}
      <div className="d-flex flex-wrap align-items-center justify-content-between mb-4 border-bottom pb-3">
        <div>
          <h2 className="fw-bold text-dark mb-1">
            <i className="bi bi-pie-chart-fill text-primary me-2"></i>
            Executive Analytics & Operational Dashboard
          </h2>
          <p className="text-muted mb-0 small">
            Real-time hospital-wide operational KPIs, clinical metrics & financial analytics
          </p>
        </div>

        <div className="d-flex align-items-center gap-2 mt-2 mt-md-0">
          <select
            className="form-select form-select-sm fw-semibold border-primary shadow-sm"
            value={period}
            onChange={(e) => setPeriod(e.target.value)}
            style={{ width: '180px' }}
          >
            <option value="TODAY">Today</option>
            <option value="YESTERDAY">Yesterday</option>
            <option value="LAST_7_DAYS">Last 7 Days</option>
            <option value="LAST_30_DAYS">Last 30 Days</option>
            <option value="CURRENT_MONTH">Current Month</option>
            <option value="PREVIOUS_MONTH">Previous Month</option>
            <option value="CURRENT_YEAR">Current Year</option>
            <option value="CUSTOM">Custom Date Range</option>
          </select>

          <button className="btn btn-sm btn-outline-primary" onClick={fetchAnalytics}>
            <i className="bi bi-arrow-clockwise me-1"></i> Refresh
          </button>
        </div>
      </div>

      {/* Custom Date Range Form */}
      {period === 'CUSTOM' && (
        <form onSubmit={handleCustomFilterSubmit} className="card card-body bg-light mb-4 shadow-sm py-2">
          <div className="row g-3 align-items-center">
            <div className="col-md-4">
              <label className="form-label small fw-bold mb-1">From Date</label>
              <input
                type="date"
                className="form-control form-control-sm"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
                required
              />
            </div>
            <div className="col-md-4">
              <label className="form-label small fw-bold mb-1">To Date</label>
              <input
                type="date"
                className="form-control form-control-sm"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
                required
              />
            </div>
            <div className="col-md-4 d-flex align-items-end">
              <button type="submit" className="btn btn-sm btn-primary w-100">
                Apply Date Range
              </button>
            </div>
          </div>
        </form>
      )}

      {error && (
        <div className="alert alert-danger d-flex align-items-center mb-4">
          <i className="bi bi-exclamation-triangle-fill me-2"></i>
          <div>{error}</div>
        </div>
      )}

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="mt-2 text-muted">Calculating real-time hospital metrics...</p>
        </div>
      ) : analytics ? (
        <>
          {/* SECTION 1: FINANCIAL HIGHLIGHTS */}
          <h5 className="fw-bold text-secondary mb-3">
            <i className="bi bi-currency-rupee me-1"></i> Financial Overview
          </h5>
          <div className="row g-3 mb-4">
            <div className="col-xl-2 col-md-4 col-6">
              <div className="card border-0 shadow-sm bg-primary text-white h-100">
                <div className="card-body p-3">
                  <div className="text-uppercase small fw-semibold opacity-75">Total Billed</div>
                  <h4 className="fw-bold mb-0 mt-1">{formatINR(analytics.totalBilled)}</h4>
                  <small className="opacity-75">Gross Invoiced</small>
                </div>
              </div>
            </div>

            <div className="col-xl-2 col-md-4 col-6">
              <div className="card border-0 shadow-sm bg-success text-white h-100">
                <div className="card-body p-3">
                  <div className="text-uppercase small fw-semibold opacity-75">Total Collected</div>
                  <h4 className="fw-bold mb-0 mt-1">{formatINR(analytics.totalCollected)}</h4>
                  <small className="opacity-75">Payments Received</small>
                </div>
              </div>
            </div>

            <div className="col-xl-2 col-md-4 col-6">
              <div className="card border-0 shadow-sm bg-warning text-dark h-100">
                <div className="card-body p-3">
                  <div className="text-uppercase small fw-semibold text-muted">Outstanding</div>
                  <h4 className="fw-bold mb-0 mt-1 text-dark">{formatINR(analytics.totalOutstanding)}</h4>
                  <small className="text-muted">Receivables Due</small>
                </div>
              </div>
            </div>

            <div className="col-xl-2 col-md-4 col-6">
              <div className="card border-0 shadow-sm bg-danger text-white h-100">
                <div className="card-body p-3">
                  <div className="text-uppercase small fw-semibold opacity-75">Total Refunded</div>
                  <h4 className="fw-bold mb-0 mt-1">{formatINR(analytics.totalRefunded)}</h4>
                  <small className="opacity-75">Processed Refunds</small>
                </div>
              </div>
            </div>

            <div className="col-xl-2 col-md-4 col-6">
              <div className="card border-0 shadow-sm bg-info text-white h-100">
                <div className="card-body p-3">
                  <div className="text-uppercase small fw-semibold opacity-75">Credit Notes</div>
                  <h4 className="fw-bold mb-0 mt-1">{formatINR(analytics.totalCreditNotes)}</h4>
                  <small className="opacity-75">Adjustments Issued</small>
                </div>
              </div>
            </div>

            <div className="col-xl-2 col-md-4 col-6">
              <div className="card border-0 shadow-sm bg-dark text-white h-100">
                <div className="card-body p-3">
                  <div className="text-uppercase small fw-semibold opacity-75">Net Collected</div>
                  <h4 className="fw-bold mb-0 mt-1 text-success">{formatINR(analytics.netCollected)}</h4>
                  <small className="opacity-75">Collection - Refund</small>
                </div>
              </div>
            </div>
          </div>

          {/* SECTION 2: PATIENT & APPOINTMENT KPIS */}
          <div className="row g-3 mb-4">
            <div className="col-lg-6">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-header bg-white fw-bold py-3 border-0">
                  <i className="bi bi-people-fill text-primary me-2"></i> Patient & Clinical Metrics
                </div>
                <div className="card-body">
                  <div className="row g-3 text-center">
                    <div className="col-4 border-end">
                      <h3 className="fw-bold text-primary mb-0">{analytics.totalRegisteredPatients}</h3>
                      <small className="text-muted">Total Registered</small>
                    </div>
                    <div className="col-4 border-end">
                      <h3 className="fw-bold text-success mb-0">{analytics.newPatients}</h3>
                      <small className="text-muted">New Registrations</small>
                    </div>
                    <div className="col-4">
                      <h3 className="fw-bold text-info mb-0">{analytics.returningPatients}</h3>
                      <small className="text-muted">Returning Patients</small>
                    </div>
                  </div>

                  <hr className="my-3" />

                  <div className="row g-3 text-center">
                    <div className="col-4 border-end">
                      <h4 className="fw-bold text-dark mb-0">{analytics.opdVisitsCount}</h4>
                      <small className="text-muted">OPD Visits</small>
                    </div>
                    <div className="col-4 border-end">
                      <h4 className="fw-bold text-primary mb-0">{analytics.ipdAdmissionsCount}</h4>
                      <small className="text-muted">IPD Admissions</small>
                    </div>
                    <div className="col-4">
                      <h4 className="fw-bold text-secondary mb-0">{analytics.dischargesCount}</h4>
                      <small className="text-muted">Discharges</small>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="col-lg-6">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-header bg-white fw-bold py-3 border-0">
                  <i className="bi bi-calendar-check-fill text-success me-2"></i> Appointment Performance
                </div>
                <div className="card-body">
                  <div className="d-flex align-items-center justify-content-between mb-2">
                    <span className="fw-semibold">Total Appointments</span>
                    <span className="badge bg-primary fs-6">{analytics.totalAppointments}</span>
                  </div>

                  <div className="progress mb-3" style={{ height: '10px' }}>
                    <div
                      className="progress-bar bg-success"
                      role="progressbar"
                      style={{ width: `${analytics.completionPercentage}%` }}
                      title={`Completed: ${analytics.completionPercentage}%`}
                    ></div>
                    <div
                      className="progress-bar bg-danger"
                      role="progressbar"
                      style={{ width: `${analytics.cancellationPercentage}%` }}
                      title={`Cancelled: ${analytics.cancellationPercentage}%`}
                    ></div>
                    <div
                      className="progress-bar bg-warning"
                      role="progressbar"
                      style={{ width: `${analytics.noShowPercentage}%` }}
                      title={`No-Show: ${analytics.noShowPercentage}%`}
                    ></div>
                  </div>

                  <div className="row g-2 text-center small">
                    <div className="col-3">
                      <div className="p-2 border rounded bg-light">
                        <div className="fw-bold text-success">{analytics.completedAppointments}</div>
                        <div className="text-muted">Completed ({analytics.completionPercentage}%)</div>
                      </div>
                    </div>
                    <div className="col-3">
                      <div className="p-2 border rounded bg-light">
                        <div className="fw-bold text-danger">{analytics.cancelledAppointments}</div>
                        <div className="text-muted">Cancelled ({analytics.cancellationPercentage}%)</div>
                      </div>
                    </div>
                    <div className="col-3">
                      <div className="p-2 border rounded bg-light">
                        <div className="fw-bold text-warning">{analytics.noShowAppointments}</div>
                        <div className="text-muted">No-Show ({analytics.noShowPercentage}%)</div>
                      </div>
                    </div>
                    <div className="col-3">
                      <div className="p-2 border rounded bg-light">
                        <div className="fw-bold text-info">{analytics.checkedInAppointments}</div>
                        <div className="text-muted">Checked In</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* SECTION 3: BEDS, ANCILLARY & PHARMACY */}
          <div className="row g-3">
            <div className="col-md-4">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-header bg-white fw-bold py-3 border-0">
                  <i className="bi bi-hospital-fill text-info me-2"></i> Bed Occupancy Metrics
                </div>
                <div className="card-body">
                  <div className="text-center mb-3">
                    <h2 className="fw-bold text-primary mb-0">{analytics.bedOccupancyPercentage}%</h2>
                    <small className="text-muted">Current Hospital Occupancy Rate</small>
                  </div>
                  <ul className="list-group list-group-flush small">
                    <li className="list-group-item d-flex justify-content-between align-items-center px-0">
                      Total Ward Beds <span className="fw-bold">{analytics.totalBeds}</span>
                    </li>
                    <li className="list-group-item d-flex justify-content-between align-items-center px-0 text-success">
                      Available Beds <span className="fw-bold">{analytics.availableBeds}</span>
                    </li>
                    <li className="list-group-item d-flex justify-content-between align-items-center px-0 text-danger">
                      Occupied Beds <span className="fw-bold">{analytics.occupiedBeds}</span>
                    </li>
                    <li className="list-group-item d-flex justify-content-between align-items-center px-0 text-warning">
                      Cleaning / Maintenance <span className="fw-bold">{analytics.cleaningBeds}</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-header bg-white fw-bold py-3 border-0">
                  <i className="bi bi-capsule text-warning me-2"></i> Pharmacy & Inventory
                </div>
                <div className="card-body">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-muted">Dispensing Transactions</span>
                    <span className="fw-bold">{analytics.dispensingTransactionsCount}</span>
                  </div>
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-muted">Total Medicines Dispensed</span>
                    <span className="fw-bold">{analytics.medicinesDispensedCount}</span>
                  </div>
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-muted">Pharmacy Revenue</span>
                    <span className="fw-bold text-success">{formatINR(analytics.pharmacyRevenue)}</span>
                  </div>
                  <hr className="my-2" />
                  <div className="d-flex justify-content-between align-items-center text-danger mb-1">
                    <span>Low Stock Items</span>
                    <span className="badge bg-danger">{analytics.lowStockMedicinesCount}</span>
                  </div>
                  <div className="d-flex justify-content-between align-items-center text-warning mb-1">
                    <span>Near Expiry Batches</span>
                    <span className="badge bg-warning text-dark">{analytics.nearExpiryBatchesCount}</span>
                  </div>
                  <div className="d-flex justify-content-between align-items-center text-secondary">
                    <span>Expired Batches</span>
                    <span className="badge bg-secondary">{analytics.expiredBatchesCount}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-header bg-white fw-bold py-3 border-0">
                  <i className="bi bi-journal-medical text-primary me-2"></i> Laboratory & Radiology
                </div>
                <div className="card-body">
                  <div className="mb-3">
                    <div className="fw-semibold small text-uppercase text-muted">Laboratory Orders</div>
                    <div className="d-flex justify-content-between align-items-center mt-1">
                      <span>Total Lab Orders: <strong>{analytics.labOrdersCount}</strong></span>
                      <span className="badge bg-success">Completed: {analytics.completedLabTestsCount}</span>
                    </div>
                  </div>
                  <hr />
                  <div>
                    <div className="fw-semibold small text-uppercase text-muted">Radiology Orders</div>
                    <div className="d-flex justify-content-between align-items-center mt-1">
                      <span>Total Radiology Orders: <strong>{analytics.radiologyOrdersCount}</strong></span>
                      <span className="badge bg-info">Completed: {analytics.completedRadiologyStudiesCount}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
};

export default ExecutiveDashboardPage;
