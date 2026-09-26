import React, { useState, useEffect } from 'react';
import billingService from '../../services/billingService';

const CreditNoteListPage = () => {
  const [creditNotes, setCreditNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchCreditNotes();
  }, []);

  const fetchCreditNotes = async () => {
    try {
      setLoading(true);
      const res = await billingService.getAllCreditNotes({ page: 0, size: 50 });
      if (res.data && res.data.success) {
        setCreditNotes(res.data.data.content || []);
      }
    } catch (err) {
      setError('Failed to fetch credit notes.');
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
            <i className="bi bi-file-earmark-diff text-danger me-2"></i>Credit Notes Directory
          </h2>
          <p className="text-muted small mb-0">Issued financial credit notes for corrections and invoice adjustments</p>
        </div>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-4 bg-white">
        <div className="card-body px-0 pb-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light text-muted small text-uppercase">
                <tr>
                  <th className="ps-4">Credit Note No.</th>
                  <th>Bill Number</th>
                  <th>Patient Name</th>
                  <th>Reason</th>
                  <th>Approved By</th>
                  <th>Amount</th>
                  <th className="text-end pe-4">Status</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="7" className="text-center py-4 text-muted">Loading credit notes...</td>
                  </tr>
                ) : creditNotes.length > 0 ? (
                  creditNotes.map((cn) => (
                    <tr key={cn.id}>
                      <td className="ps-4 fw-semibold text-primary">{cn.creditNoteNumber}</td>
                      <td className="fw-medium text-dark">{cn.billNumber}</td>
                      <td>{cn.patientName}</td>
                      <td className="small text-muted">{cn.reason}</td>
                      <td>{cn.approvedBy || cn.createdBy}</td>
                      <td className="fw-bold text-danger">{formatCurrency(cn.amount)}</td>
                      <td className="text-end pe-4">
                        <span className="badge bg-success">{cn.status}</span>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" className="text-center py-4 text-muted">No credit notes found.</td>
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

export default CreditNoteListPage;
