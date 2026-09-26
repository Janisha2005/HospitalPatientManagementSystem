import React, { useState, useEffect } from 'react';
import pharmacyService from '../../services/pharmacyService';
import patientService from '../../services/patientService';

const PharmacyReturnPage = () => {
  const [returns, setReturns] = useState([]);
  const [dispensings, setDispensings] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [activeTab, setActiveTab] = useState('PATIENT');

  const [patientForm, setPatientForm] = useState({
    dispensingId: '',
    quantityReturned: 1,
    isRestocked: true,
    reason: 'Unused medicine returned by patient',
    remarks: ''
  });

  const [supplierForm, setSupplierForm] = useState({
    supplierId: '',
    medicineId: '',
    batchId: '',
    quantityReturned: 1,
    reason: 'Expired / Quality issue',
    remarks: ''
  });

  useEffect(() => {
    fetchReturns();
    fetchDropdowns();
  }, []);

  const fetchReturns = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getReturns(0, 100);
      setReturns(res.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load return history');
    } finally {
      setLoading(false);
    }
  };

  const fetchDropdowns = async () => {
    try {
      const [dispRes, supRes] = await Promise.all([
        pharmacyService.getDispensings(0, 50),
        pharmacyService.getActiveSuppliers()
      ]);
      setDispensings(dispRes.data?.content || []);
      setSuppliers(supRes.data || []);
    } catch (err) {
      console.error('Failed to load dropdowns', err);
    }
  };

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹0.00';
    return Number(val).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
  };

  const handlePatientSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const dispensing = dispensings.find(d => d.id === Number(patientForm.dispensingId));
      if (!dispensing) throw new Error('Please select a valid dispensing record');

      const payload = {
        dispensingId: dispensing.id,
        patientId: dispensing.patientId,
        medicineId: dispensing.medicineId,
        batchId: dispensing.batchId,
        quantityReturned: Number(patientForm.quantityReturned),
        isRestocked: Boolean(patientForm.isRestocked),
        reason: patientForm.reason,
        remarks: patientForm.remarks
      };

      await pharmacyService.processPatientReturn(payload);
      setSuccess('Patient return processed successfully!');
      fetchReturns();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to process patient return');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Pharmacy Returns & Restocking</h2>
          <p className="text-muted small">Process patient medicine returns and supplier returns with inventory restocking options</p>
        </div>
      </div>

      {error && <div className="alert alert-danger mb-4">{error}</div>}
      {success && <div className="alert alert-success mb-4">{success}</div>}

      <div className="row g-4">
        {/* Form Card */}
        <div className="col-lg-5">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-header bg-white py-3">
              <ul className="nav nav-pills card-header-pills">
                <li className="nav-item">
                  <button
                    className={`nav-link fw-bold ${activeTab === 'PATIENT' ? 'active' : ''}`}
                    onClick={() => setActiveTab('PATIENT')}
                  >
                    Patient Return
                  </button>
                </li>
              </ul>
            </div>
            <div className="card-body p-4">
              {activeTab === 'PATIENT' && (
                <form onSubmit={handlePatientSubmit}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Select Original Dispensing *</label>
                    <select
                      className="form-select"
                      required
                      value={patientForm.dispensingId}
                      onChange={(e) => setPatientForm({ ...patientForm, dispensingId: e.target.value })}
                    >
                      <option value="">-- Choose Dispensing Record --</option>
                      {dispensings.map(d => (
                        <option key={d.id} value={d.id}>
                          {d.dispensingId} - {d.patientName} ({d.medicineName}, Qty: {d.dispensedQuantity})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="mb-3">
                    <label className="form-label fw-bold">Return Quantity *</label>
                    <input
                      type="number"
                      className="form-control"
                      required
                      min="1"
                      value={patientForm.quantityReturned}
                      onChange={(e) => setPatientForm({ ...patientForm, quantityReturned: e.target.value })}
                    />
                  </div>

                  <div className="form-check form-switch mb-3">
                    <input
                      type="checkbox"
                      className="form-check-input"
                      id="isRestocked"
                      checked={patientForm.isRestocked}
                      onChange={(e) => setPatientForm({ ...patientForm, isRestocked: e.target.checked })}
                    />
                    <label className="form-check-label fw-bold" htmlFor="isRestocked">
                      Restock into Active Inventory (If un-opened)
                    </label>
                  </div>

                  <div className="mb-3">
                    <label className="form-label fw-bold">Reason for Return *</label>
                    <input
                      type="text"
                      className="form-control"
                      required
                      placeholder="e.g. Treatment changed / unused"
                      value={patientForm.reason}
                      onChange={(e) => setPatientForm({ ...patientForm, reason: e.target.value })}
                    />
                  </div>

                  <button type="submit" className="btn btn-primary w-100 fw-bold py-2" disabled={loading}>
                    {loading ? 'Processing Return...' : 'Process Patient Return'}
                  </button>
                </form>
              )}
            </div>
          </div>
        </div>

        {/* Returns History Table */}
        <div className="col-lg-7">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-header bg-white py-3">
              <h5 className="mb-0 fw-bold text-dark">
                <i className="bi bi-arrow-counterclockwise me-2 text-primary"></i> Pharmacy Returns Audit Log
              </h5>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="table-light small text-uppercase">
                    <tr>
                      <th>Return ID</th>
                      <th>Type</th>
                      <th>Patient / Supplier</th>
                      <th>Return Date</th>
                      <th>Refund Amount</th>
                      <th>Processed By</th>
                    </tr>
                  </thead>
                  <tbody>
                    {returns.length > 0 ? (
                      returns.map((r) => (
                        <tr key={r.id}>
                          <td className="fw-bold text-primary">{r.returnId}</td>
                          <td>
                            <span className={`badge ${r.returnType === 'PATIENT_RETURN' ? 'bg-info text-dark' : 'bg-warning text-dark'}`}>
                              {r.returnType}
                            </span>
                          </td>
                          <td className="fw-bold">{r.patientName || r.supplierName || 'N/A'}</td>
                          <td>{r.returnDate}</td>
                          <td className="fw-bold text-success">{formatCurrency(r.refundAmount)}</td>
                          <td>{r.processedBy || 'Pharmacist'}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="6" className="text-center py-4 text-muted">No return records processed yet.</td>
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

export default PharmacyReturnPage;
