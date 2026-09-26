import React, { useState, useEffect } from 'react';
import billingService from '../../services/billingService';

const RefundListPage = () => {
  const [refunds, setRefunds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchRefunds();
  }, []);

  const fetchRefunds = async () => {
    try {
      setLoading(true);
      const res = await billingService.getAllRefunds({ page: 0, size: 50 });
      if (res.data && res.data.success) {
        setRefunds(res.data.data.content || []);
      }
    } catch (err) {
      setError('Failed to fetch refund records.');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return (amount || 0).toLocaleString('en-IN', {
      style: 'currency',
      currency: 'INR'
    });
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold mb-1" style={{ color: '#1e293b' }}>
            <i className="bi bi-arrow-return-left text-warning me-2"></i>Refunds & Adjustments Log
          </h2>
          <p className="text-muted small mb-0">Record of all issued patient refunds, pharmacy return credits, and billing adjustments</p>
        </div>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-4 bg-white">
        <div className="card-body px-0 pb-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light text-muted small text-uppercase">
                <tr>
                  <th className="ps-4">Refund No.</th>
                  <th>Bill Number</th>
                  <th>Patient Name</th>
                  <th>Refund Date</th>
                  <th>Method</th>
                  <th>Reason</th>
                  <th>Amount</th>
                  <th className="text-end pe-4">Status</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-muted">Loading refunds...</td>
                  </tr>
                ) : refunds.length > 0 ? (
                  refunds.map((r) => (
                    <tr key={r.id}>
                      <td className="ps-4 fw-semibold text-primary">{r.refundNumber}</td>
                      <td className="fw-medium text-dark">{r.billNumber}</td>
                      <td>{r.patientName}</td>
                      <td>{r.refundDate ? r.refundDate.split('T')[0] : ''}</td>
                      <td><span className="badge bg-warning bg-opacity-10 text-dark border">{r.refundMethod}</span></td>
                      <td className="small text-muted">{r.reason}</td>
                      <td className="fw-bold text-danger">{formatCurrency(r.refundAmount)}</td>
                      <td className="text-end pe-4">
                        <span className="badge bg-success">{r.status}</span>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-muted">No refund records found.</td>
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

export default RefundListPage;
