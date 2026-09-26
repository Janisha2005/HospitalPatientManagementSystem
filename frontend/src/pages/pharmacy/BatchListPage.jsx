import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import pharmacyService from '../../services/pharmacyService';

const BatchListPage = () => {
  const [searchParams] = useSearchParams();
  const filterMedicineId = searchParams.get('medicineId');

  const [batches, setBatches] = useState([]);
  const [medicines, setMedicines] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);

  const [formData, setFormData] = useState({
    medicineId: '',
    supplierId: '',
    batchNumber: '',
    manufacturingDate: '',
    expiryDate: '',
    purchaseRate: '',
    mrp: '',
    sellingRate: '',
    quantityAvailable: 100,
    storageLocation: 'Shelf A-1'
  });

  useEffect(() => {
    fetchBatches();
    fetchMedicinesAndSuppliers();
  }, [filterMedicineId]);

  const fetchBatches = async () => {
    setLoading(true);
    try {
      if (filterMedicineId) {
        const res = await pharmacyService.getBatchesByMedicine(filterMedicineId);
        setBatches(res.data || []);
      } else {
        const res = await pharmacyService.getBatches(0, 100);
        setBatches(res.data.content || []);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch batch records');
    } finally {
      setLoading(false);
    }
  };

  const fetchMedicinesAndSuppliers = async () => {
    try {
      const [medRes, supRes] = await Promise.all([
        pharmacyService.getActiveMedicines(),
        pharmacyService.getActiveSuppliers()
      ]);
      setMedicines(medRes.data || []);
      setSuppliers(supRes.data || []);
    } catch (err) {
      console.error('Failed to load dropdown data', err);
    }
  };

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹0.00';
    return Number(val).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'AVAILABLE': return <span className="badge bg-success">AVAILABLE</span>;
      case 'NEAR_EXPIRY': return <span className="badge bg-warning text-dark">NEAR EXPIRY</span>;
      case 'EXPIRED': return <span className="badge bg-danger">EXPIRED</span>;
      case 'BLOCKED': return <span className="badge bg-dark">BLOCKED</span>;
      case 'DEPLETED': return <span className="badge bg-secondary">DEPLETED</span>;
      default: return <span className="badge bg-info">{status}</span>;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        medicineId: Number(formData.medicineId),
        supplierId: formData.supplierId ? Number(formData.supplierId) : null,
        purchaseRate: Number(formData.purchaseRate),
        mrp: Number(formData.mrp),
        sellingRate: Number(formData.sellingRate || formData.mrp),
        quantityReceived: Number(formData.quantityAvailable),
        quantityAvailable: Number(formData.quantityAvailable)
      };
      await pharmacyService.createBatch(payload);
      setShowModal(false);
      fetchBatches();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to register batch');
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Batch Registry & Expiry Management</h2>
          <p className="text-muted small">Batch numbers, manufacturing/expiry dates, storage locations & FEFO priority</p>
        </div>
        <button className="btn btn-primary shadow-sm" onClick={() => setShowModal(true)}>
          <i className="bi bi-plus-lg me-1"></i> Register New Batch
        </button>
      </div>

      {error && <div className="alert alert-danger mb-4">{error}</div>}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-uppercase">
                  <tr>
                    <th>Batch ID</th>
                    <th>Batch Number</th>
                    <th>Medicine</th>
                    <th>Supplier</th>
                    <th>Mfg. Date</th>
                    <th>Expiry Date</th>
                    <th>Purchase Rate</th>
                    <th>MRP / Selling</th>
                    <th>Available Qty</th>
                    <th>Storage</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {batches.length > 0 ? (
                    batches.map((b) => (
                      <tr key={b.id}>
                        <td className="fw-bold text-primary">{b.batchId}</td>
                        <td className="fw-bold text-dark">{b.batchNumber}</td>
                        <td>
                          <div className="fw-bold">{b.medicineName}</div>
                          <div className="text-muted small">{b.medicineCode}</div>
                        </td>
                        <td>{b.supplierName || 'General / Unknown'}</td>
                        <td>{b.manufacturingDate || 'N/A'}</td>
                        <td className="fw-bold text-danger">{b.expiryDate}</td>
                        <td>{formatCurrency(b.purchaseRate)}</td>
                        <td>{formatCurrency(b.mrp)}</td>
                        <td><span className="badge bg-light text-dark border fs-6">{b.quantityAvailable}</span></td>
                        <td>{b.storageLocation || 'Aisle 1'}</td>
                        <td>{getStatusBadge(b.status)}</td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="11" className="text-center py-4 text-muted">No medicine batches found.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Register Batch Modal */}
      {showModal && (
        <div className="modal d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <div className="modal-header">
                <h5 className="modal-title fw-bold">Register New Batch</h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label fw-bold">Medicine *</label>
                      <select
                        className="form-select"
                        required
                        value={formData.medicineId}
                        onChange={(e) => setFormData({ ...formData, medicineId: e.target.value })}
                      >
                        <option value="">-- Select Medicine --</option>
                        {medicines.map(m => (
                          <option key={m.id} value={m.id}>{m.medicineName} ({m.medicineCode})</option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label fw-bold">Supplier</label>
                      <select
                        className="form-select"
                        value={formData.supplierId}
                        onChange={(e) => setFormData({ ...formData, supplierId: e.target.value })}
                      >
                        <option value="">-- Select Supplier --</option>
                        {suppliers.map(s => (
                          <option key={s.id} value={s.id}>{s.supplierName}</option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-4">
                      <label className="form-label fw-bold">Batch Number *</label>
                      <input
                        type="text"
                        className="form-control"
                        required
                        placeholder="e.g. BATCH-2026-X"
                        value={formData.batchNumber}
                        onChange={(e) => setFormData({ ...formData, batchNumber: e.target.value })}
                      />
                    </div>

                    <div className="col-md-4">
                      <label className="form-label fw-bold">Manufacturing Date</label>
                      <input
                        type="date"
                        className="form-control"
                        value={formData.manufacturingDate}
                        onChange={(e) => setFormData({ ...formData, manufacturingDate: e.target.value })}
                      />
                    </div>

                    <div className="col-md-4">
                      <label className="form-label fw-bold">Expiry Date *</label>
                      <input
                        type="date"
                        className="form-control"
                        required
                        value={formData.expiryDate}
                        onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
                      />
                    </div>

                    <div className="col-md-4">
                      <label className="form-label fw-bold">Purchase Rate (₹) *</label>
                      <input
                        type="number"
                        step="0.01"
                        className="form-control"
                        required
                        placeholder="0.00"
                        value={formData.purchaseRate}
                        onChange={(e) => setFormData({ ...formData, purchaseRate: e.target.value })}
                      />
                    </div>

                    <div className="col-md-4">
                      <label className="form-label fw-bold">MRP (₹) *</label>
                      <input
                        type="number"
                        step="0.01"
                        className="form-control"
                        required
                        placeholder="0.00"
                        value={formData.mrp}
                        onChange={(e) => setFormData({ ...formData, mrp: e.target.value })}
                      />
                    </div>

                    <div className="col-md-4">
                      <label className="form-label fw-bold">Selling Rate (₹)</label>
                      <input
                        type="number"
                        step="0.01"
                        className="form-control"
                        placeholder="0.00"
                        value={formData.sellingRate}
                        onChange={(e) => setFormData({ ...formData, sellingRate: e.target.value })}
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label fw-bold">Initial Available Qty *</label>
                      <input
                        type="number"
                        className="form-control"
                        required
                        min="1"
                        value={formData.quantityAvailable}
                        onChange={(e) => setFormData({ ...formData, quantityAvailable: e.target.value })}
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label fw-bold">Storage Location</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. Shelf A-1 / Refrigerator 2"
                        value={formData.storageLocation}
                        onChange={(e) => setFormData({ ...formData, storageLocation: e.target.value })}
                      />
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary">Save Batch</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BatchListPage;
