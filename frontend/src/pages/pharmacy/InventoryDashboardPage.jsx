import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import pharmacyService from '../../services/pharmacyService';

const InventoryDashboardPage = () => {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getInventoryDashboard();
      setDashboard(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load inventory metrics');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹0.00';
    return Number(val).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
  };

  if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>;
  if (error) return <div className="alert alert-danger m-4">{error}</div>;

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Inventory Metrics & Valuation</h2>
          <p className="text-muted small">Live stock valuation, ledger history, movement auditing & expired stock value</p>
        </div>
        <div>
          <Link to="/pharmacy/adjust-stock" className="btn btn-warning shadow-sm me-2">
            <i className="bi bi-sliders me-1"></i> Stock Adjustment
          </Link>
          <Link to="/pharmacy/batches" className="btn btn-outline-primary shadow-sm">
            <i className="bi bi-boxes me-1"></i> Batch Registry
          </Link>
        </div>
      </div>

      {/* Financial Valuation & Metric Cards */}
      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-dark text-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-white-50 small fw-bold text-uppercase">Total Stock Valuation</div>
                <div className="fs-3 fw-bold mb-0">{formatCurrency(dashboard?.totalStockValue)}</div>
              </div>
              <i className="bi bi-currency-rupee fs-1 text-white-50"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-info text-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-white-50 small fw-bold text-uppercase">Total Inventory Items</div>
                <div className="display-6 fw-bold mb-0">{dashboard?.totalStockItems || 0}</div>
              </div>
              <i className="bi bi-box-seam fs-1 text-white-50"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-warning text-dark">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-dark-50 small fw-bold text-uppercase">Low Stock Medicines</div>
                <div className="display-6 fw-bold mb-0">{dashboard?.lowStockItemsCount || 0}</div>
              </div>
              <i className="bi bi-exclamation-triangle fs-1 opacity-75"></i>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card border-0 shadow-sm rounded-3 p-3 bg-danger text-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="text-white-50 small fw-bold text-uppercase">Expired Stock Valuation</div>
                <div className="fs-3 fw-bold mb-0">{formatCurrency(dashboard?.expiredStockValue)}</div>
              </div>
              <i className="bi bi-x-circle-fill fs-1 text-white-50"></i>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Movements & Transactions Table */}
      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
          <h5 className="mb-0 fw-bold text-dark">
            <i className="bi bi-clock-history me-2 text-primary"></i> Recent Stock Movement History
          </h5>
          <span className="badge bg-light text-dark border">Audited Stock Ledger</span>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light small text-uppercase">
                <tr>
                  <th>Transaction ID</th>
                  <th>Date & Time</th>
                  <th>Medicine</th>
                  <th>Batch Number</th>
                  <th>Movement Type</th>
                  <th>Quantity</th>
                  <th>Unit Rate</th>
                  <th>User</th>
                  <th>Remarks</th>
                </tr>
              </thead>
              <tbody>
                {dashboard?.recentMovements && dashboard.recentMovements.length > 0 ? (
                  dashboard.recentMovements.map((tx) => (
                    <tr key={tx.id}>
                      <td className="fw-bold text-primary">{tx.transactionId}</td>
                      <td>{new Date(tx.transactionDatetime).toLocaleString('en-IN')}</td>
                      <td className="fw-bold">{tx.medicineName}</td>
                      <td><span className="badge bg-light text-dark border">{tx.batchNumber || 'N/A'}</span></td>
                      <td>
                        <span className={`badge ${
                          tx.transactionType.includes('DISPENSING') || tx.transactionType.includes('OUT') || tx.transactionType.includes('DAMAGE')
                            ? 'bg-danger-subtle text-danger'
                            : 'bg-success-subtle text-success'
                        }`}>
                          {tx.transactionType}
                        </span>
                      </td>
                      <td className="fw-bold">{tx.quantity}</td>
                      <td>{formatCurrency(tx.unitCost)}</td>
                      <td>{tx.performedBy || 'System'}</td>
                      <td className="text-muted small">{tx.remarks || 'N/A'}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="9" className="text-center py-4 text-muted">No recent stock movements recorded.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default InventoryDashboardPage;
