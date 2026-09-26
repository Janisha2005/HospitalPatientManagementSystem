import React, { useState, useEffect } from 'react';
import pharmacyService from '../../services/pharmacyService';
import patientService from '../../services/patientService';

const PrescriptionDispensingPage = () => {
  const [dispensings, setDispensings] = useState([]);
  const [patients, setPatients] = useState([]);
  const [medicines, setMedicines] = useState([]);
  const [batches, setBatches] = useState([]);
  const [selectedMedicineId, setSelectedMedicineId] = useState('');

  const [formData, setFormData] = useState({
    patientId: '',
    medicineId: '',
    batchId: '',
    prescribedQuantity: 10,
    dispensedQuantity: 10,
    remarks: ''
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    fetchDispensings();
    fetchDropdowns();
  }, []);

  const fetchDispensings = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getDispensings(0, 100);
      setDispensings(res.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load dispensing records');
    } finally {
      setLoading(false);
    }
  };

  const fetchDropdowns = async () => {
    try {
      const [patRes, medRes] = await Promise.all([
        patientService.getAllPatients(0, 100),
        pharmacyService.getActiveMedicines()
      ]);
      setPatients(patRes.data?.content || patRes.data || []);
      setMedicines(medRes.data || []);
    } catch (err) {
      console.error('Failed to load patients/medicines', err);
    }
  };

  const handleMedicineChange = async (e) => {
    const medId = e.target.value;
    setSelectedMedicineId(medId);
    setFormData(prev => ({ ...prev, medicineId: medId, batchId: '' }));

    if (medId) {
      try {
        const res = await pharmacyService.getBatchesByMedicine(medId);
        setBatches(res.data || []);
      } catch (err) {
        console.error('Failed to load batches', err);
      }
    } else {
      setBatches([]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const payload = {
        patientId: Number(formData.patientId),
        medicineId: Number(formData.medicineId),
        batchId: formData.batchId ? Number(formData.batchId) : null, // If null, FEFO is auto-used by backend
        prescribedQuantity: Number(formData.prescribedQuantity),
        dispensedQuantity: Number(formData.dispensedQuantity),
        remarks: formData.remarks
      };

      await pharmacyService.dispenseMedicine(payload);
      setSuccess('Prescription dispensed successfully using FEFO batch algorithm!');
      fetchDispensings();
      setFormData({
        patientId: '',
        medicineId: '',
        batchId: '',
        prescribedQuantity: 10,
        dispensedQuantity: 10,
        remarks: ''
      });
      setSelectedMedicineId('');
      setBatches([]);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to dispense prescription');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Prescription Dispensing & FEFO Fulfillment</h2>
          <p className="text-muted small">Fulfill OPD/IPD prescriptions using automatic First Expiry, First Out (FEFO) batch allocation</p>
        </div>
      </div>

      {error && <div className="alert alert-danger mb-4">{error}</div>}
      {success && <div className="alert alert-success mb-4">{success}</div>}

      <div className="row g-4">
        {/* Dispense Form Card */}
        <div className="col-lg-5">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-header bg-primary text-white py-3">
              <h5 className="mb-0 fw-bold"><i className="bi bi-capsule me-2"></i> Dispense Medication</h5>
            </div>
            <div className="card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label fw-bold">Select Patient *</label>
                  <select
                    className="form-select"
                    required
                    value={formData.patientId}
                    onChange={(e) => setFormData({ ...formData, patientId: e.target.value })}
                  >
                    <option value="">-- Choose Patient --</option>
                    {patients.map(p => (
                      <option key={p.id} value={p.id}>{p.firstName} {p.lastName} ({p.patientId})</option>
                    ))}
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label fw-bold">Select Medicine *</label>
                  <select
                    className="form-select"
                    required
                    value={selectedMedicineId}
                    onChange={handleMedicineChange}
                  >
                    <option value="">-- Choose Medicine --</option>
                    {medicines.map(m => (
                      <option key={m.id} value={m.id}>
                        {m.medicineName} ({m.strength}) • Stock: {m.availableStock || 0}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label fw-bold">Batch Selection (FEFO Auto-Suggested)</label>
                  <select
                    className="form-select"
                    value={formData.batchId}
                    onChange={(e) => setFormData({ ...formData, batchId: e.target.value })}
                  >
                    <option value="">-- Auto-select earliest expiring batch (FEFO) --</option>
                    {batches.map(b => (
                      <option key={b.id} value={b.id}>
                        {b.batchNumber} (Available: {b.quantityAvailable}, Exp: {b.expiryDate})
                      </option>
                    ))}
                  </select>
                  <div className="form-text text-muted">
                    If left blank, the system automatically allocates the earliest expiring valid batch.
                  </div>
                </div>

                <div className="row g-2 mb-3">
                  <div className="col-6">
                    <label className="form-label fw-bold">Prescribed Qty *</label>
                    <input
                      type="number"
                      className="form-control"
                      required
                      min="1"
                      value={formData.prescribedQuantity}
                      onChange={(e) => setFormData({ ...formData, prescribedQuantity: e.target.value })}
                    />
                  </div>
                  <div className="col-6">
                    <label className="form-label fw-bold">Dispensing Qty *</label>
                    <input
                      type="number"
                      className="form-control"
                      required
                      min="1"
                      value={formData.dispensedQuantity}
                      onChange={(e) => setFormData({ ...formData, dispensedQuantity: e.target.value })}
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label fw-bold">Dispensing Remarks</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="e.g. 1 Tablet after meals daily"
                    value={formData.remarks}
                    onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                  />
                </div>

                <button type="submit" className="btn btn-primary w-100 fw-bold py-2" disabled={loading}>
                  {loading ? 'Processing Dispense...' : 'Confirm & Dispense Stock'}
                </button>
              </form>
            </div>
          </div>
        </div>

        {/* Dispensing History Table */}
        <div className="col-lg-7">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-header bg-white py-3">
              <h5 className="mb-0 fw-bold text-dark">
                <i className="bi bi-clock-history me-2 text-primary"></i> Pharmacy Dispensing Log
              </h5>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="table-light small text-uppercase">
                    <tr>
                      <th>Dispense ID</th>
                      <th>Patient</th>
                      <th>Medicine</th>
                      <th>Batch No.</th>
                      <th>Dispensed Qty</th>
                      <th>Dispensed By</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dispensings.length > 0 ? (
                      dispensings.map((d) => (
                        <tr key={d.id}>
                          <td className="fw-bold text-primary">{d.dispensingId}</td>
                          <td>
                            <div className="fw-bold">{d.patientName}</div>
                            <div className="text-muted small">{d.patientIdCode}</div>
                          </td>
                          <td className="fw-bold">{d.medicineName}</td>
                          <td><span className="badge bg-light text-dark border">{d.batchNumber}</span></td>
                          <td className="fw-bold">{d.dispensedQuantity}</td>
                          <td>{d.dispensedBy || 'Pharmacist'}</td>
                          <td>
                            <span className={`badge ${d.status === 'FULLY_DISPENSED' ? 'bg-success' : 'bg-warning text-dark'}`}>
                              {d.status}
                            </span>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="7" className="text-center py-4 text-muted">No dispensing history recorded yet.</td>
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

export default PrescriptionDispensingPage;
