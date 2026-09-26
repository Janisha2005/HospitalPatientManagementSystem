import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import billingService from '../../services/billingService';

const BillInvoicePage = () => {
  const { id } = useParams();
  const [bill, setBill] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchBill();
  }, [id]);

  const fetchBill = async () => {
    try {
      setLoading(true);
      const res = await billingService.getBillById(id);
      if (res.data && res.data.success) {
        setBill(res.data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const formatCurrency = (amount) => {
    return (amount || 0).toLocaleString('en-IN', {
      style: 'currency',
      currency: 'INR'
    });
  };

  if (loading || !bill) {
    return (
      <div className="d-flex justify-content-center align-items-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading Printable Invoice...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4 d-print-none">
        <Link to={`/billing/bills/${id}`} className="btn btn-outline-secondary rounded-pill px-3">&larr; Back to Bill Details</Link>
        <button className="btn btn-primary rounded-pill px-4" onClick={handlePrint}>
          <i className="bi bi-printer me-2"></i>Print Official Invoice
        </button>
      </div>

      <div className="card border shadow-sm p-4 p-md-5 rounded-4 bg-white" id="printableInvoice">
        {/* Hospital Header */}
        <div className="d-flex justify-content-between align-items-start border-bottom pb-4 mb-4">
          <div>
            <h2 className="fw-bold text-primary mb-1">PULSE HOSPITAL & RESEARCH CENTRE</h2>
            <div className="small text-muted">Multi-Specialty Care & Diagnostic Centre</div>
            <div className="small text-muted">24 Anna Salai, Guindy, Chennai - 600032, Tamil Nadu</div>
            <div className="small text-muted">GSTIN: 33AAAAA0000A1Z5 | Phone: +91 44 2250 0000</div>
          </div>
          <div className="text-end">
            <h4 className="fw-bold text-dark mb-1">OFFICIAL INVOICE</h4>
            <div className="fw-bold text-primary fs-5">{bill.billNumber}</div>
            <div className="small text-muted">Date: {bill.billDate}</div>
            <div className="small text-muted">Status: <span className="fw-bold text-dark">{bill.status}</span></div>
          </div>
        </div>

        {/* Patient Details */}
        <div className="row mb-4">
          <div className="col-6">
            <div className="p-3 bg-light rounded-3 border">
              <div className="small text-uppercase text-muted fw-bold mb-1">Billed To</div>
              <div className="fw-bold text-dark fs-5">{bill.patientName}</div>
              <div className="small text-muted">Patient Code: {bill.patientCode}</div>
              <div className="small text-muted">Patient ID: #{bill.patientId}</div>
            </div>
          </div>
          <div className="col-6">
            <div className="p-3 bg-light rounded-3 border">
              <div className="small text-uppercase text-muted fw-bold mb-1">Invoice Info</div>
              <div className="small text-dark mb-1">Bill Type: <span className="fw-semibold">{bill.billType}</span></div>
              <div className="small text-dark mb-1">Due Date: <span className="fw-semibold">{bill.dueDate || 'On Receipt'}</span></div>
              <div className="small text-dark">Billed By: <span className="fw-semibold">{bill.createdBy}</span></div>
            </div>
          </div>
        </div>

        {/* Line Items */}
        <table className="table table-bordered align-middle mb-4">
          <thead className="table-light text-uppercase small">
            <tr>
              <th>#</th>
              <th>Item Description</th>
              <th>Charge Code</th>
              <th className="text-center">Qty</th>
              <th className="text-end">Unit Rate</th>
              <th className="text-end">Discount</th>
              <th className="text-end">Line Total</th>
            </tr>
          </thead>
          <tbody>
            {bill.items && bill.items.map((item, idx) => (
              <tr key={idx}>
                <td>{idx + 1}</td>
                <td className="fw-medium text-dark">{item.description}</td>
                <td className="small text-muted">{item.chargeCode || '-'}</td>
                <td className="text-center">{item.quantity}</td>
                <td className="text-end">{formatCurrency(item.unitRate)}</td>
                <td className="text-end text-danger">{formatCurrency(item.discountAmount)}</td>
                <td className="text-end fw-bold">{formatCurrency(item.lineTotal)}</td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* Financial Summary */}
        <div className="row justify-content-end">
          <div className="col-5">
            <div className="table-responsive">
              <table className="table table-sm table-borderless">
                <tbody>
                  <tr>
                    <td className="text-muted">Subtotal:</td>
                    <td className="text-end fw-medium">{formatCurrency(bill.subtotal)}</td>
                  </tr>
                  <tr>
                    <td className="text-muted">Discount:</td>
                    <td className="text-end text-danger">- {formatCurrency(bill.discountAmount)}</td>
                  </tr>
                  <tr>
                    <td className="text-muted">Tax Amount:</td>
                    <td className="text-end fw-medium">{formatCurrency(bill.taxAmount)}</td>
                  </tr>
                  <tr className="border-top border-2">
                    <td className="fw-bold text-dark fs-6">Grand Total:</td>
                    <td className="text-end fw-bold text-primary fs-5">{formatCurrency(bill.grandTotal)}</td>
                  </tr>
                  <tr>
                    <td className="fw-semibold text-success">Paid Amount:</td>
                    <td className="text-end fw-semibold text-success">{formatCurrency(bill.paidAmount)}</td>
                  </tr>
                  <tr className="border-top">
                    <td className="fw-bold text-danger">Outstanding Amount:</td>
                    <td className="text-end fw-bold text-danger">{formatCurrency(bill.outstandingAmount)}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Footer Signature */}
        <div className="row mt-5 pt-4 border-top">
          <div className="col-6">
            <div className="small text-muted">Thank you for choosing Pulse Hospital.</div>
            <div className="small text-muted">Computer generated tax invoice receipt.</div>
          </div>
          <div className="col-6 text-end">
            <div className="border-bottom d-inline-block px-5 pb-2 fw-bold text-dark">Authorized Billing Signatory</div>
            <div className="small text-muted mt-1">Pulse Hospital Accounts</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BillInvoicePage;
