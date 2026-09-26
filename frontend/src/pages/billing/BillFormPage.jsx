import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import billingService from '../../services/billingService';
import patientService from '../../services/patientService';

const BillFormPage = () => {
  const navigate = useNavigate();
  const [patients, setPatients] = useState([]);
  const [charges, setCharges] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    patientId: '',
    billType: 'OPD',
    notes: ''
  });

  const [items, setItems] = useState([
    { chargeCode: '', description: '', sourceType: 'CONSULTATION', quantity: 1, unitRate: 0, discountAmount: 0, taxPercentage: 0 }
  ]);

  useEffect(() => {
    fetchPatientsAndCharges();
  }, []);

  const fetchPatientsAndCharges = async () => {
    try {
      const pRes = await patientService.getPatients({ page: 0, size: 100 });
      if (pRes.data && pRes.data.success) {
        setPatients(pRes.data.data.content || []);
      }
      const cRes = await billingService.getAllCharges({ page: 0, size: 100 });
      if (cRes.data && cRes.data.success) {
        setCharges(cRes.data.data.content || []);
      }
    } catch (err) {
      setError('Failed to fetch patients or charge master list.');
    }
  };

  const handleAddItem = () => {
    setItems([
      ...items,
      { chargeCode: '', description: '', sourceType: 'CONSULTATION', quantity: 1, unitRate: 0, discountAmount: 0, taxPercentage: 0 }
    ]);
  };

  const handleRemoveItem = (index) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = value;

    if (field === 'chargeCode') {
      const selectedCharge = charges.find(c => c.chargeCode === value);
      if (selectedCharge) {
        newItems[index].description = selectedCharge.chargeName;
        newItems[index].unitRate = selectedCharge.baseRate;
        newItems[index].taxPercentage = selectedCharge.taxPercentage || 0;
      }
    }
    setItems(newItems);
  };

  const calculateSubtotal = () => {
    return items.reduce((sum, item) => sum + (Number(item.unitRate || 0) * Number(item.quantity || 1)), 0);
  };

  const calculateDiscount = () => {
    return items.reduce((sum, item) => sum + Number(item.discountAmount || 0), 0);
  };

  const calculateGrandTotal = () => {
    const sub = calculateSubtotal();
    const disc = calculateDiscount();
    return Math.max(0, sub - disc);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.patientId) {
      alert('Please select a patient.');
      return;
    }

    try {
      setLoading(true);
      const payload = {
        patientId: Number(formData.patientId),
        billType: formData.billType,
        notes: formData.notes,
        items: items.map(i => ({
          ...i,
          quantity: Number(i.quantity),
          unitRate: Number(i.unitRate),
          discountAmount: Number(i.discountAmount),
          taxPercentage: Number(i.taxPercentage)
        }))
      };

      const res = await billingService.createDraftBill(payload);
      if (res.data && res.data.success) {
        navigate(`/billing/bills/${res.data.data.id}`);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create draft bill.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold mb-1" style={{ color: '#1e293b' }}>
            <i className="bi bi-file-earmark-plus text-primary me-2"></i>Create Draft Bill
          </h2>
          <p className="text-muted small mb-0">Build a new custom draft invoice with itemized charges</p>
        </div>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="card border-0 shadow-sm rounded-4 bg-white mb-4">
          <div className="card-body p-4">
            <h5 className="fw-bold mb-3">1. Patient & Invoice Details</h5>
            <div className="row g-3 mb-3">
              <div className="col-12 col-md-6">
                <label className="form-label fw-medium">Select Patient *</label>
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
              <div className="col-12 col-md-6">
                <label className="form-label fw-medium">Bill Type *</label>
                <select
                  className="form-select"
                  value={formData.billType}
                  onChange={(e) => setFormData({ ...formData, billType: e.target.value })}
                >
                  <option value="OPD">OPD</option>
                  <option value="IPD">IPD</option>
                  <option value="PHARMACY">PHARMACY</option>
                  <option value="LABORATORY">LABORATORY</option>
                  <option value="RADIOLOGY">RADIOLOGY</option>
                  <option value="EMERGENCY">EMERGENCY</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </div>
            </div>
            <div className="mb-2">
              <label className="form-label fw-medium">Bill Notes / Remarks</label>
              <textarea
                className="form-control"
                rows="2"
                placeholder="Optional notes or clinical reference details..."
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              ></textarea>
            </div>
          </div>
        </div>

        {/* Bill Items Section */}
        <div className="card border-0 shadow-sm rounded-4 bg-white mb-4">
          <div className="card-body p-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h5 className="fw-bold mb-0">2. Charge Items</h5>
              <button type="button" className="btn btn-outline-primary btn-sm rounded-pill px-3" onClick={handleAddItem}>
                <i className="bi bi-plus-lg me-1"></i>Add Item Line
              </button>
            </div>

            {items.map((item, index) => (
              <div key={index} className="row g-2 mb-3 align-items-center p-3 rounded-3 bg-light border">
                <div className="col-12 col-md-3">
                  <label className="form-label small text-muted">Preset Charge Rate</label>
                  <select
                    className="form-select form-select-sm"
                    value={item.chargeCode}
                    onChange={(e) => handleItemChange(index, 'chargeCode', e.target.value)}
                  >
                    <option value="">Custom Charge</option>
                    {charges.map(c => (
                      <option key={c.id} value={c.chargeCode}>{c.chargeName} (₹{c.baseRate})</option>
                    ))}
                  </select>
                </div>
                <div className="col-12 col-md-3">
                  <label className="form-label small text-muted">Item Description *</label>
                  <input
                    type="text"
                    className="form-control form-control-sm"
                    required
                    placeholder="Description"
                    value={item.description}
                    onChange={(e) => handleItemChange(index, 'description', e.target.value)}
                  />
                </div>
                <div className="col-4 col-md-1">
                  <label className="form-label small text-muted">Qty *</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control form-control-sm"
                    value={item.quantity}
                    onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
                  />
                </div>
                <div className="col-4 col-md-2">
                  <label className="form-label small text-muted">Unit Rate (₹) *</label>
                  <input
                    type="number"
                    step="0.01"
                    className="form-control form-control-sm"
                    value={item.unitRate}
                    onChange={(e) => handleItemChange(index, 'unitRate', e.target.value)}
                  />
                </div>
                <div className="col-4 col-md-2">
                  <label className="form-label small text-muted">Discount (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    className="form-control form-control-sm"
                    value={item.discountAmount}
                    onChange={(e) => handleItemChange(index, 'discountAmount', e.target.value)}
                  />
                </div>
                <div className="col-12 col-md-1 text-end">
                  {items.length > 1 && (
                    <button type="button" className="btn btn-outline-danger btn-sm rounded-circle p-1" onClick={() => handleRemoveItem(index)}>
                      <i className="bi bi-trash"></i>
                    </button>
                  )}
                </div>
              </div>
            ))}

            {/* Calculations Summary */}
            <div className="row justify-content-end mt-4">
              <div className="col-12 col-md-4">
                <div className="p-3 rounded-3 bg-light border">
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-muted">Subtotal:</span>
                    <span className="fw-medium">₹{calculateSubtotal().toFixed(2)}</span>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-muted">Discount:</span>
                    <span className="text-danger">- ₹{calculateDiscount().toFixed(2)}</span>
                  </div>
                  <hr className="my-2" />
                  <div className="d-flex justify-content-between">
                    <span className="fw-bold text-dark">Grand Total:</span>
                    <span className="fw-bold text-primary fs-5">₹{calculateGrandTotal().toFixed(2)}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="d-flex justify-content-end gap-2">
          <button type="button" className="btn btn-light rounded-pill px-4" onClick={() => navigate('/billing/bills')}>Cancel</button>
          <button type="submit" className="btn btn-primary rounded-pill px-4" disabled={loading}>
            {loading ? 'Creating Bill...' : 'Create Draft Bill'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BillFormPage;
