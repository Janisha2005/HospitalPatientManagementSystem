import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import billingService from '../../services/billingService';

const BillDetailPage = () => {
  const { id } = useParams();
  const [bill, setBill] = useState(null);
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Payment Modal State
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [paymentData, setPaymentData] = useState({
    amount: '',
    paymentMethod: 'CASH',
    referenceNumber: '',
    remarks: ''
  });

  // Refund Modal State
  const [showRefundModal, setShowRefundModal] = useState(false);
  const [refundData, setRefundData] = useState({
    refundAmount: '',
    refundMethod: 'CASH',
    reason: ''
  });

  useEffect(() => {
    fetchBillAndPayments();
  }, [id]);

  const fetchBillAndPayments = async () => {
    try {
      setLoading(true);
      const bRes = await billingService.getBillById(id);
      if (bRes.data && bRes.data.success) {
        setBill(bRes.data.data);
        setPaymentData(prev => ({ ...prev, amount: bRes.data.data.outstandingAmount }));
        setRefundData(prev => ({ ...prev, refundAmount: bRes.data.data.paidAmount }));
      }
      const pRes = await billingService.getPaymentsByBillId(id);
      if (pRes.data && pRes.data.success) {
        setPayments(pRes.data.data || []);
      }
    } catch (err) {
      setError('Failed to fetch bill details.');
    } finally {
      setLoading(false);
    }
  };

  const handleFinalize = async () => {
    try {
      await billingService.finalizeBill(id);
      fetchBillAndPayments();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to finalize bill.');
    }
  };

  const handleRecordPayment = async (e) => {
    e.preventDefault();
    try {
      await billingService.recordPayment({
        billId: Number(id),
        amount: Number(paymentData.amount),
        paymentMethod: paymentData.paymentMethod,
        referenceNumber: paymentData.referenceNumber,
        remarks: paymentData.remarks
      });
      setShowPaymentModal(false);
      fetchBillAndPayments();
    } catch (err) {
      alert(err.response?.data?.message || 'Error recording payment.');
    }
  };

  const handleProcessRefund = async (e) => {
    e.preventDefault();
    try {
      await billingService.processRefund({
        billId: Number(id),
        refundAmount: Number(refundData.refundAmount),
        refundMethod: refundData.refundMethod,
        reason: refundData.reason
      });
      setShowRefundModal(false);
      fetchBillAndPayments();
    } catch (err) {
      alert(err.response?.data?.message || 'Error processing refund.');
    }
  };

  const formatCurrency = (amount) => {
    return (amount || 0).toLocaleString('en-IN', {
      style: 'currency',
      currency: 'INR'
    });
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading Bill Details...</span>
        </div>
      </div>
    );
  }

  if (error || !bill) {
    return (
      <div className="container-fluid py-4">
        <div className="alert alert-danger shadow-sm">{error || 'Bill not found.'}</div>
        <Link to="/billing/bills" className="btn btn-outline-secondary rounded-pill px-3">&larr; Back to Bills</Link>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <div className="d-flex align-items-center gap-2 mb-1">
            <h2 className="fw-bold mb-0" style={{ color: '#1e293b' }}>{bill.billNumber}</h2>
            <span className={`badge ${
              bill.status === 'PAID' ? 'bg-success' :
              bill.status === 'PARTIALLY_PAID' ? 'bg-warning text-dark' :
              bill.status === 'GENERATED' ? 'bg-info text-dark' :
              bill.status === 'CANCELLED' ? 'bg-danger' : 'bg-secondary'
            }`}>
              {bill.status}
            </span>
          </div>
          <p className="text-muted small mb-0">Patient: <span className="fw-semibold text-dark">{bill.patientName}</span> ({bill.patientCode}) | Date: {bill.billDate}</p>
        </div>
        <div className="d-flex gap-2">
          {bill.status === 'DRAFT' && (
            <button className="btn btn-success rounded-pill px-3" onClick={handleFinalize}>
              <i className="bi bi-check-lg me-1"></i>Finalize Invoice
            </button>
          )}
          {(bill.status === 'GENERATED' || bill.status === 'PARTIALLY_PAID') && bill.outstandingAmount > 0 && (
            <button className="btn btn-primary rounded-pill px-3" onClick={() => setShowPaymentModal(true)}>
              <i className="bi bi-credit-card me-1"></i>Record Payment
            </button>
          )}
          {bill.paidAmount > 0 && (
            <button className="btn btn-outline-warning rounded-pill px-3" onClick={() => setShowRefundModal(true)}>
              <i className="bi bi-arrow-return-left me-1"></i>Issue Refund
            </button>
          )}
          <Link to={`/billing/bills/${id}/invoice`} className="btn btn-outline-secondary rounded-pill px-3">
            <i className="bi bi-printer me-1"></i>Print Printable Receipt
          </Link>
        </div>
      </div>

      <div className="row g-4">
        {/* Bill Line Items Table */}
        <div className="col-12 col-lg-8">
          <div className="card border-0 shadow-sm rounded-4 bg-white mb-4">
            <div className="card-header bg-transparent border-0 pt-4 px-4 pb-0">
              <h5 className="fw-bold mb-0 text-dark">Invoice Items Summary</h5>
            </div>
            <div className="card-body px-0 pb-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="table-light text-muted small text-uppercase">
                    <tr>
                      <th className="ps-4">Item Description</th>
                      <th>Category</th>
                      <th>Qty</th>
                      <th>Rate</th>
                      <th>Discount</th>
                      <th className="text-end pe-4">Line Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {bill.items && bill.items.length > 0 ? (
                      bill.items.map((item, idx) => (
                        <tr key={idx}>
                          <td className="ps-4 fw-medium text-dark">{item.description}</td>
                          <td><span className="badge bg-light text-dark border">{item.sourceType}</span></td>
                          <td>{item.quantity}</td>
                          <td>{formatCurrency(item.unitRate)}</td>
                          <td className="text-danger">{formatCurrency(item.discountAmount)}</td>
                          <td className="text-end pe-4 fw-bold text-dark">{formatCurrency(item.lineTotal)}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="6" className="text-center py-4 text-muted">No line items recorded.</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* Payment History */}
          <div className="card border-0 shadow-sm rounded-4 bg-white">
            <div className="card-header bg-transparent border-0 pt-4 px-4 pb-0">
              <h5 className="fw-bold mb-0 text-dark">Payment Transactions History</h5>
            </div>
            <div className="card-body px-0 pb-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="table-light text-muted small text-uppercase">
                    <tr>
                      <th className="ps-4">Receipt No.</th>
                      <th>Date</th>
                      <th>Method</th>
                      <th>Reference No</th>
                      <th>Amount</th>
                      <th className="text-end pe-4">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {payments.length > 0 ? (
                      payments.map((p) => (
                        <tr key={p.id}>
                          <td className="ps-4 fw-semibold text-primary">{p.paymentNumber}</td>
                          <td>{p.paymentDate ? p.paymentDate.split('T')[0] : ''}</td>
                          <td><span className="badge bg-info bg-opacity-10 text-info border">{p.paymentMethod}</span></td>
                          <td className="small text-muted">{p.referenceNumber || '-'}</td>
                          <td className="fw-bold text-success">{formatCurrency(p.amount)}</td>
                          <td className="text-end pe-4">
                            <span className={`badge ${p.status === 'COMPLETED' ? 'bg-success' : 'bg-danger'}`}>
                              {p.status}
                            </span>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="6" className="text-center py-4 text-muted">No payment history.</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        {/* Financial Summary Card */}
        <div className="col-12 col-lg-4">
          <div className="card border-0 shadow-sm rounded-4 bg-white">
            <div className="card-header bg-transparent border-0 pt-4 px-4 pb-0">
              <h5 className="fw-bold mb-0 text-dark">Financial Summary</h5>
            </div>
            <div className="card-body p-4">
              <div className="d-flex justify-content-between mb-2">
                <span className="text-muted">Subtotal:</span>
                <span className="fw-medium text-dark">{formatCurrency(bill.subtotal)}</span>
              </div>
              <div className="d-flex justify-content-between mb-2">
                <span className="text-muted">Total Discount:</span>
                <span className="text-danger">- {formatCurrency(bill.discountAmount)}</span>
              </div>
              <div className="d-flex justify-content-between mb-2">
                <span className="text-muted">Tax Amount:</span>
                <span className="fw-medium text-dark">{formatCurrency(bill.taxAmount)}</span>
              </div>
              <div className="d-flex justify-content-between mb-2">
                <span className="text-muted">Round Off:</span>
                <span className="small text-muted">{formatCurrency(bill.roundOff)}</span>
              </div>
              <hr />
              <div className="d-flex justify-content-between mb-3">
                <span className="fw-bold text-dark fs-5">Grand Total:</span>
                <span className="fw-bold text-primary fs-5">{formatCurrency(bill.grandTotal)}</span>
              </div>
              <div className="d-flex justify-content-between mb-2">
                <span className="fw-semibold text-success">Paid Amount:</span>
                <span className="fw-semibold text-success">{formatCurrency(bill.paidAmount)}</span>
              </div>
              {bill.refundedAmount > 0 && (
                <div className="d-flex justify-content-between mb-2">
                  <span className="fw-semibold text-warning">Refunded Amount:</span>
                  <span className="fw-semibold text-warning">{formatCurrency(bill.refundedAmount)}</span>
                </div>
              )}
              <div className="d-flex justify-content-between pt-2 border-top">
                <span className="fw-bold text-danger">Outstanding Balance:</span>
                <span className="fw-bold text-danger fs-5">{formatCurrency(bill.outstandingAmount)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Payment Modal */}
      {showPaymentModal && (
        <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content rounded-4 border-0 shadow">
              <div className="modal-header border-0 pb-0">
                <h5 className="modal-title fw-bold">Record Payment</h5>
                <button type="button" className="btn-close" onClick={() => setShowPaymentModal(false)}></button>
              </div>
              <form onSubmit={handleRecordPayment}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label fw-medium">Payment Amount (₹) *</label>
                    <input
                      type="number"
                      step="0.01"
                      className="form-control"
                      required
                      max={bill.outstandingAmount}
                      value={paymentData.amount}
                      onChange={(e) => setPaymentData({ ...paymentData, amount: e.target.value })}
                    />
                    <div className="form-text">Outstanding balance: {formatCurrency(bill.outstandingAmount)}</div>
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-medium">Payment Method *</label>
                    <select
                      className="form-select"
                      value={paymentData.paymentMethod}
                      onChange={(e) => setPaymentData({ ...paymentData, paymentMethod: e.target.value })}
                    >
                      <option value="CASH">CASH</option>
                      <option value="UPI">UPI</option>
                      <option value="CARD">CARD</option>
                      <option value="BANK_TRANSFER">BANK TRANSFER</option>
                      <option value="CHEQUE">CHEQUE</option>
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-medium">Reference Number (UPI / Txn ID / Cheque No)</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. UPI-1029384857"
                      value={paymentData.referenceNumber}
                      onChange={(e) => setPaymentData({ ...paymentData, referenceNumber: e.target.value })}
                    />
                  </div>
                </div>
                <div className="modal-footer border-0 pt-0">
                  <button type="button" className="btn btn-light rounded-pill px-3" onClick={() => setShowPaymentModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary rounded-pill px-4">Submit Payment</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Refund Modal */}
      {showRefundModal && (
        <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content rounded-4 border-0 shadow">
              <div className="modal-header border-0 pb-0">
                <h5 className="modal-title fw-bold text-danger">Issue Refund</h5>
                <button type="button" className="btn-close" onClick={() => setShowRefundModal(false)}></button>
              </div>
              <form onSubmit={handleProcessRefund}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label fw-medium">Refund Amount (₹) *</label>
                    <input
                      type="number"
                      step="0.01"
                      className="form-control"
                      required
                      max={bill.paidAmount}
                      value={refundData.refundAmount}
                      onChange={(e) => setRefundData({ ...refundData, refundAmount: e.target.value })}
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-medium">Refund Method *</label>
                    <select
                      className="form-select"
                      value={refundData.refundMethod}
                      onChange={(e) => setRefundData({ ...refundData, refundMethod: e.target.value })}
                    >
                      <option value="CASH">CASH</option>
                      <option value="UPI">UPI</option>
                      <option value="BANK_TRANSFER">BANK TRANSFER</option>
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-medium">Refund Reason *</label>
                    <textarea
                      className="form-control"
                      rows="2"
                      required
                      placeholder="Reason for financial refund..."
                      value={refundData.reason}
                      onChange={(e) => setRefundData({ ...refundData, reason: e.target.value })}
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer border-0 pt-0">
                  <button type="button" className="btn btn-light rounded-pill px-3" onClick={() => setShowRefundModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-danger rounded-pill px-4">Process Refund</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BillDetailPage;
