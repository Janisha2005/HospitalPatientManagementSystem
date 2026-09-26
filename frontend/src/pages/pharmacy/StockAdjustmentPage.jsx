import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import pharmacyService from '../../services/pharmacyService';

const StockAdjustmentPage = () => {
  const navigate = useNavigate();

  const [medicines, setMedicines] = useState([]);
  const [batches, setBatches] = useState([]);
  const [selectedMedicineId, setSelectedMedicineId] = useState('');

  const [formData, setFormData] = useState({
    batchId: '',
    adjustmentQuantity: 10,
    direction: 'IN',
    reason: 'Physical count difference',
    remarks: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    fetchMedicines();
  }, []);

  const fetchMedicines = async () => {
    try {
      const res = await pharmacyService.getActiveMedicines();
      setMedicines(res.data || []);
    } catch (err) {
      console.error('Failed to load medicines', err);
    }
  };

  const handleMedicineChange = async (e) => {
    const medId = e.target.value;
    setSelectedMedicineId(medId);
    setFormData(prev => ({ ...prev, batchId: '' }));

    if (medId) {
      try {
        const res = await pharmacyService.getBatchesByMedicine(medId);
        setBatches(res.data || []);
      } catch (err) {
        console.error('Failed to load batches for medicine', err);
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
        medicineId: Number(selectedMedicineId),
        batchId: Number(formData.batchId),
        adjustmentQuantity: Number(formData.adjustmentQuantity),
        direction: formData.direction,
        reason: formData.reason,
        remarks: formData.remarks
      };

      await pharmacyService.adjustStock(payload);
      setSuccess('Stock adjustment completed successfully! Inventory transaction logged.');
      setTimeout(() => {
        navigate('/pharmacy/inventory/dashboard');
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to adjust stock');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-4" style={{ maxWidth: '700px' }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Authorized Stock Adjustment</h2>
          <p className="text-muted small">Record physical count differences, damaged or lost stock with mandatory audit logs</p>
        </div>
        <Link to="/pharmacy/inventory/dashboard" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Inventory Metrics
        </Link>
      </div>

      {error && <div className="alert alert-danger mb-4">{error}</div>}
      {success && <div className="alert alert-success mb-4">{success}</div>}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-12">
                <label className="form-label fw-bold">Select Medicine *</label>
                <select
                  className="form-select"
                  required
                  value={selectedMedicineId}
                  onChange={handleMedicineChange}
                >
                  <option value="">-- Choose Medicine --</option>
                  {medicines.map(m => (
                    <option key={m.id} value={m.id}>{m.medicineName} ({m.medicineCode})</option>
                  ))}
                </select>
              </div>

              <div className="col-12">
                <label className="form-label fw-bold">Select Batch *</label>
                <select
                  className="form-select"
                  required
                  disabled={!selectedMedicineId}
                  value={formData.batchId}
                  onChange={(e) => setFormData({ ...formData, batchId: e.target.value })}
                >
                  <option value="">-- Choose Batch --</option>
                  {batches.map(b => (
                    <option key={b.id} value={b.id}>
                      {b.batchNumber} (Available: {b.quantityAvailable}, Expiry: {b.expiryDate})
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Adjustment Direction *</label>
                <select
                  className="form-select"
                  required
                  value={formData.direction}
                  onChange={(e) => setFormData({ ...formData, direction: e.target.value })}
                >
                  <option value="IN">STOCK IN (+ Increase Stock)</option>
                  <option value="OUT">STOCK OUT (- Decrease Stock)</option>
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Adjustment Quantity *</label>
                <input
                  type="number"
                  className="form-control"
                  required
                  min="1"
                  value={formData.adjustmentQuantity}
                  onChange={(e) => setFormData({ ...formData, adjustmentQuantity: e.target.value })}
                />
              </div>

              <div className="col-12">
                <label className="form-label fw-bold">Reason for Adjustment *</label>
                <select
                  className="form-select"
                  required
                  value={formData.reason}
                  onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                >
                  <option value="Physical count difference">Physical Count Difference</option>
                  <option value="Damaged stock">Damaged Stock</option>
                  <option value="Expired stock">Expired Stock</option>
                  <option value="Lost stock">Lost Stock</option>
                  <option value="Data correction">Data Correction</option>
                  <option value="Other">Other Reason</option>
                </select>
              </div>

              <div className="col-12">
                <label className="form-label fw-bold">Audit Remarks / Notes</label>
                <textarea
                  className="form-control"
                  rows="3"
                  placeholder="Provide audit justification..."
                  value={formData.remarks}
                  onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                ></textarea>
              </div>

              <div className="col-12 mt-4 d-flex justify-content-end gap-2">
                <Link to="/pharmacy/inventory/dashboard" className="btn btn-secondary">Cancel</Link>
                <button type="submit" className="btn btn-warning fw-bold" disabled={loading}>
                  {loading ? 'Processing...' : 'Confirm Stock Adjustment'}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default StockAdjustmentPage;
