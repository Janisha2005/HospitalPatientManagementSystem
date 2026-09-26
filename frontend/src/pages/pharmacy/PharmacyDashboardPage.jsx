import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import pharmacyService from '../../services/pharmacyService';

const PharmacyDashboardPage = () => {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getPharmacyDashboard();
      setDashboard(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load pharmacy dashboard');
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
          <h2 className="h3 fw-bold text-dark mb-1">Pharmacy Dashboard</h2>
          <p className="text-muted small">Live stock levels, prescription dispensing, low-stock alerts & expiry tracking</p>
        </div>
        <div>
          <Link to="/pharmacy/dispense" className="btn btn-primary shadow-sm me-2">
            <i className="bi bi-capsule me-1"></i> Dispense Prescription
          </Link>
          <Link to="/pharmacy/goods-receipts/new" className="btn btn-outline-success shadow-sm me-2">
            <i className="bi bi-box-seam me-1"></i> Receive Stock (GRN)
          </Link>
          <Link to="/pharmacy/inventory/dashboard" className="btn btn-outline-dark shadow-sm">
            <i className="bi bi-graph-up me-1"></i> Inventory Metrics
          </Link>
        </div>
      </div>

      {/* Metric Cards */}
      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-primary text-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-white-50 small fw-bold text-uppercase">Total Medicine Catalog</div>
                <div className="display-6 fw-bold mb-0">{dashboard?.totalMedicines || 0}</div>
              </div>
              <i className="bi bi-prescription2 fs-1 text-white-50"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-warning text-dark">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-dark-50 small fw-bold text-uppercase">Low Stock Alert</div>
                <div className="display-6 fw-bold mb-0">{dashboard?.lowStockCount || 0}</div>
              </div>
              <i className="bi bi-exclamation-triangle-fill fs-1 opacity-75"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-danger text-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-white-50 small fw-bold text-uppercase">Near Expiry (30 Days)</div>
                <div className="display-6 fw-bold mb-0">{dashboard?.nearExpiryCount || 0}</div>
              </div>
              <i className="bi bi-clock-history fs-1 text-white-50"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-success text-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-white-50 small fw-bold text-uppercase">Today's Dispensings</div>
                <div className="display-6 fw-bold mb-0">{dashboard?.todayDispensingCount || 0}</div>
              </div>
              <i className="bi bi-check-circle-fill fs-1 text-white-50"></i>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-4">
        {/* Low Stock Alert Table */}
        <div className="col-lg-6">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
              <h5 className="mb-0 fw-bold text-warning text-dark">
                <i className="bi bi-exclamation-circle-fill me-2 text-warning"></i> Low Stock Alert Items
              </h5>
              <Link to="/pharmacy/inventory" className="btn btn-sm btn-outline-secondary">View All Inventory</Link>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="table-light small">
                    <tr>
                      <th>Medicine</th>
                      <th>Category</th>
                      <th>Available</th>
                      <th>Reorder Level</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dashboard?.lowStockMedicines && dashboard.lowStockMedicines.length > 0 ? (
                      dashboard.lowStockMedicines.map((m) => (
                        <tr key={m.id}>
                          <td>
                            <div className="fw-bold">{m.medicineName}</div>
                            <div className="text-muted small">{m.medicineCode} ({m.strength})</div>
                          </td>
                          <td>{m.category?.categoryName || 'General'}</td>
                          <td><span className="badge bg-danger">{m.availableStock} {m.unit}</span></td>
                          <td>{m.reorderLevel}</td>
                          <td><span className="badge bg-warning text-dark">Low Stock</span></td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="5" className="text-center py-4 text-muted">No low-stock alerts. All medicines adequately stocked!</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        {/* Near Expiry Batches Table */}
        <div className="col-lg-6">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
              <h5 className="mb-0 fw-bold text-danger">
                <i className="bi bi-alarm-fill me-2 text-danger"></i> Batches Expiring Soon
              </h5>
              <Link to="/pharmacy/batches" className="btn btn-sm btn-outline-secondary">View Batches</Link>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="table-light small">
                    <tr>
                      <th>Batch No.</th>
                      <th>Medicine</th>
                      <th>Expiry Date</th>
                      <th>Available Qty</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dashboard?.nearExpiryBatches && dashboard.nearExpiryBatches.length > 0 ? (
                      dashboard.nearExpiryBatches.map((b) => (
                        <tr key={b.id}>
                          <td className="fw-bold text-primary">{b.batchNumber}</td>
                          <td>{b.medicineName}</td>
                          <td className="text-danger fw-bold">{b.expiryDate}</td>
                          <td>{b.quantityAvailable}</td>
                          <td>
                            <Link to="/pharmacy/adjust-stock" className="btn btn-sm btn-outline-warning">
                              Adjust
                            </Link>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="5" className="text-center py-4 text-muted">No batches near expiry (within 30 days).</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PharmacyDashboardPage;
