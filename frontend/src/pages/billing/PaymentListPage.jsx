import React, { useState, useEffect } from 'react';
import billingService from '../../services/billingService';

const PaymentListPage = () => {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPayments();
  }, []);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      const res = await billingService.getAllPayments({ page: 0, size: 50 });
      if (res.data && res.data.success) {
        setPayments(res.data.data.content || []);
      }
    } catch (err) {
      setError('Failed to fetch payment receipts.');
    } finally {
      setLoading(false);
    }
  };

  const handleReverse = async (id) => {
    const reason = prompt('Enter reason for payment reversal:');
    if (!reason) return;
    try {
      await billingService.reversePayment(id, reason);
      fetchPayments();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to reverse payment.');
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
            <i className="bi bi-wallet2 text-success me-2"></i>Payment Receipts & Transactions
          </h2>
          <p className="text-muted small mb-0">Master transaction log of cash, UPI, card, and bank transfer collections</p>
        </div>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-4 bg-white">
        <div className="card-body px-0 pb-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light text-muted small text-uppercase">
                <tr>
                  <th className="ps-4">Payment No.</th>
                  <th>Bill Number</th>
                  <th>Patient Name</th>
                  <th>Payment Date</th>
                  <th>Method</th>
                  <th>Ref Number</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th className="text-end pe-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="9" className="text-center py-4 text-muted">Loading payments...</td>
                  </tr>
                ) : payments.length > 0 ? (
                  payments.map((p) => (
                    <tr key={p.id}>
                      <td className="ps-4 fw-semibold text-primary">{p.paymentNumber}</td>
                      <td className="fw-medium text-dark">{p.billNumber}</td>
                      <td>{p.patientName}</td>
                      <td>{p.paymentDate ? p.paymentDate.split('T')[0] : ''}</td>
                      <td><span className="badge bg-info bg-opacity-10 text-info border">{p.paymentMethod}</span></td>
                      <td className="small text-muted">{p.referenceNumber || '-'}</td>
                      <td className="fw-bold text-success">{formatCurrency(p.amount)}</td>
                      <td>
                        <span className={`badge ${p.status === 'COMPLETED' ? 'bg-success' : 'bg-danger'}`}>
                          {p.status}
                        </span>
                      </td>
                      <td className="text-end pe-4">
                        {p.status === 'COMPLETED' && (
                          <button className="btn btn-sm btn-outline-danger" onClick={() => handleReverse(p.id)}>
                            Reverse
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="9" className="text-center py-4 text-muted">No payment records found.</td>
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

export default PaymentListPage;
