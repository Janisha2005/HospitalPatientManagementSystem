import React, { useState, useEffect } from 'react';
import billingService from '../../services/billingService';
import patientService from '../../services/patientService';

const PatientLedgerPage = () => {
  const [patients, setPatients] = useState([]);
  const [selectedPatientId, setSelectedPatientId] = useState('');
  const [ledgerEntries, setLedgerEntries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPatients();
  }, []);

  const fetchPatients = async () => {
    try {
      const res = await patientService.getPatients({ page: 0, size: 100 });
      if (res.data && res.data.success) {
        const list = res.data.data.content || [];
        setPatients(list);
        if (list.length > 0) {
          setSelectedPatientId(list[0].id);
          fetchLedger(list[0].id);
        }
      }
    } catch (err) {
      setError('Failed to fetch patients list.');
    }
  };

  const fetchLedger = async (patientId) => {
    try {
      setLoading(true);
      const res = await billingService.getPatientLedger(patientId);
      if (res.data && res.data.success) {
        setLedgerEntries(res.data.data || []);
      }
    } catch (err) {
      setError('Failed to fetch patient financial ledger.');
    } finally {
      setLoading(false);
    }
  };

  const handlePatientSelect = (e) => {
    const pId = e.target.value;
    setSelectedPatientId(pId);
    if (pId) {
      fetchLedger(pId);
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
            <i className="bi bi-book text-info me-2"></i>Patient Financial Ledger
          </h2>
          <p className="text-muted small mb-0">Chronological financial ledger tracking all debit bills, credit payments, and running balance</p>
        </div>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-4 bg-white mb-4">
        <div className="card-body p-3">
          <div className="row g-3 align-items-center">
            <div className="col-12 col-md-6 col-lg-4">
              <label className="form-label small fw-bold text-muted mb-1">Select Patient Account</label>
              <select
                className="form-select"
                value={selectedPatientId}
                onChange={handlePatientSelect}
              >
                <option value="">-- Choose Patient --</option>
                {patients.map(p => (
                  <option key={p.id} value={p.id}>{p.firstName} {p.lastName} ({p.patientId})</option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-4 bg-white">
        <div className="card-body px-0 pb-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light text-muted small text-uppercase">
                <tr>
                  <th className="ps-4">Ledger No.</th>
                  <th>Date & Time</th>
                  <th>Entry Type</th>
                  <th>Description</th>
                  <th>Debit (Billed +)</th>
                  <th>Credit (Paid -)</th>
                  <th className="text-end pe-4">Running Balance</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="7" className="text-center py-4 text-muted">Loading financial ledger...</td>
                  </tr>
                ) : ledgerEntries.length > 0 ? (
                  ledgerEntries.map((e) => (
                    <tr key={e.id}>
                      <td className="ps-4 fw-semibold text-primary">{e.ledgerNumber}</td>
                      <td>{e.entryDate ? e.entryDate.replace('T', ' ').substring(0, 16) : ''}</td>
                      <td>
                        <span className={`badge ${
                          e.entryType === 'BILL' ? 'bg-danger bg-opacity-10 text-danger border' :
                          e.entryType === 'PAYMENT' ? 'bg-success bg-opacity-10 text-success border' :
                          e.entryType === 'REFUND' ? 'bg-warning bg-opacity-10 text-dark border' : 'bg-secondary'
                        }`}>
                          {e.entryType}
                        </span>
                      </td>
                      <td className="fw-medium text-dark">{e.description}</td>
                      <td className="text-danger fw-medium">{e.debitAmount > 0 ? formatCurrency(e.debitAmount) : '-'}</td>
                      <td className="text-success fw-medium">{e.creditAmount > 0 ? formatCurrency(e.creditAmount) : '-'}</td>
                      <td className="text-end pe-4 fw-bold text-dark">{formatCurrency(e.balanceAfter)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" className="text-center py-4 text-muted">No ledger entries found for selected patient.</td>
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

export default PatientLedgerPage;
