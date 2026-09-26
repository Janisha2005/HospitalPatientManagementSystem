import React, { useState, useEffect } from 'react';
import pharmacyService from '../../services/pharmacyService';

const PurchaseOrderListPage = () => {
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [medicines, setMedicines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);

  const [formData, setFormData] = useState({
    supplierId: '',
    expectedDeliveryDate: '',
    notes: '',
    items: [
      { medicineId: '', orderedQuantity: 100, unitCost: 10.00, taxPercentage: 12.00, discountAmount: 0 }
    ]
  });

  useEffect(() => {
    fetchPurchaseOrders();
    fetchSuppliersAndMedicines();
  }, []);

  const fetchPurchaseOrders = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getPurchaseOrders(0, 100);
      setPurchaseOrders(res.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load purchase orders');
    } finally {
      setLoading(false);
    }
  };

  const fetchSuppliersAndMedicines = async () => {
    try {
      const [supRes, medRes] = await Promise.all([
        pharmacyService.getActiveSuppliers(),
        pharmacyService.getActiveMedicines()
      ]);
      setSuppliers(supRes.data || []);
      setMedicines(medRes.data || []);
    } catch (err) {
      console.error('Failed to load dropdown data', err);
    }
  };

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹0.00';
    return Number(val).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
  };

  const handleAddItem = () => {
    setFormData({
      ...formData,
      items: [...formData.items, { medicineId: '', orderedQuantity: 100, unitCost: 10.00, taxPercentage: 12.00, discountAmount: 0 }]
    });
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...formData.items];
    newItems[index][field] = value;
    setFormData({ ...formData, items: newItems });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        supplierId: Number(formData.supplierId),
        expectedDeliveryDate: formData.expectedDeliveryDate || null,
        notes: formData.notes,
        items: formData.items.map(item => ({
          medicineId: Number(item.medicineId),
          orderedQuantity: Number(item.orderedQuantity),
          unitCost: Number(item.unitCost),
          taxPercentage: Number(item.taxPercentage),
          discountAmount: Number(item.discountAmount)
        }))
      };
      await pharmacyService.createPurchaseOrder(payload);
      setShowModal(false);
      fetchPurchaseOrders();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create purchase order');
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Procurement & Purchase Orders</h2>
          <p className="text-muted small">Manage supplier orders, line items, delivery tracking & procurement status</p>
        </div>
        <button className="btn btn-primary shadow-sm" onClick={() => setShowModal(true)}>
          <i className="bi bi-plus-lg me-1"></i> Create Purchase Order
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
                    <th>PO ID</th>
                    <th>Supplier</th>
                    <th>Order Date</th>
                    <th>Expected Delivery</th>
                    <th>Grand Total</th>
                    <th>Status</th>
                    <th>Created By</th>
                  </tr>
                </thead>
                <tbody>
                  {purchaseOrders.length > 0 ? (
                    purchaseOrders.map((po) => (
                      <tr key={po.id}>
                        <td className="fw-bold text-primary">{po.purchaseOrderId}</td>
                        <td className="fw-bold">{po.supplierName}</td>
                        <td>{po.orderDate}</td>
                        <td>{po.expectedDeliveryDate || 'N/A'}</td>
                        <td className="fw-bold text-dark">{formatCurrency(po.grandTotal)}</td>
                        <td>
                          <span className={`badge ${
                            po.status === 'RECEIVED' ? 'bg-success' :
                            po.status === 'PARTIALLY_RECEIVED' ? 'bg-info' :
                            po.status === 'PLACED' ? 'bg-primary' : 'bg-secondary'
                          }`}>
                            {po.status}
                          </span>
                        </td>
                        <td>{po.createdBy || 'System'}</td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="7" className="text-center py-4 text-muted">No purchase orders created.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Create Purchase Order Modal */}
      {showModal && (
        <div className="modal d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <div className="modal-header">
                <h5 className="modal-title fw-bold">Create Purchase Order</h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  <div className="row g-3 mb-3">
                    <div className="col-md-6">
                      <label className="form-label fw-bold">Supplier *</label>
                      <select
                        className="form-select"
                        required
                        value={formData.supplierId}
                        onChange={(e) => setFormData({ ...formData, supplierId: e.target.value })}
                      >
                        <option value="">-- Select Supplier --</option>
                        {suppliers.map(s => (
                          <option key={s.id} value={s.id}>{s.supplierName}</option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label fw-bold">Expected Delivery Date</label>
                      <input
                        type="date"
                        className="form-control"
                        value={formData.expectedDeliveryDate}
                        onChange={(e) => setFormData({ ...formData, expectedDeliveryDate: e.target.value })}
                      />
                    </div>
                  </div>

                  <h6 className="fw-bold border-bottom pb-2 mt-4">Order Items</h6>
                  {formData.items.map((item, idx) => (
                    <div key={idx} className="row g-2 mb-2 align-items-center bg-light p-2 rounded">
                      <div className="col-md-4">
                        <select
                          className="form-select form-select-sm"
                          required
                          value={item.medicineId}
                          onChange={(e) => handleItemChange(idx, 'medicineId', e.target.value)}
                        >
                          <option value="">-- Select Medicine --</option>
                          {medicines.map(m => (
                            <option key={m.id} value={m.id}>{m.medicineName}</option>
                          ))}
                        </select>
                      </div>
                      <div className="col-md-2">
                        <input
                          type="number"
                          className="form-control form-control-sm"
                          placeholder="Qty"
                          required
                          min="1"
                          value={item.orderedQuantity}
                          onChange={(e) => handleItemChange(idx, 'orderedQuantity', e.target.value)}
                        />
                      </div>
                      <div className="col-md-3">
                        <input
                          type="number"
                          step="0.01"
                          className="form-control form-control-sm"
                          placeholder="Unit Cost (₹)"
                          required
                          value={item.unitCost}
                          onChange={(e) => handleItemChange(idx, 'unitCost', e.target.value)}
                        />
                      </div>
                      <div className="col-md-3">
                        <input
                          type="number"
                          step="0.01"
                          className="form-control form-control-sm"
                          placeholder="Tax %"
                          value={item.taxPercentage}
                          onChange={(e) => handleItemChange(idx, 'taxPercentage', e.target.value)}
                        />
                      </div>
                    </div>
                  ))}
                  <button type="button" className="btn btn-sm btn-outline-primary mt-2" onClick={handleAddItem}>
                    + Add Item Row
                  </button>

                  <div className="mt-3">
                    <label className="form-label fw-bold">Notes</label>
                    <textarea
                      className="form-control"
                      rows="2"
                      placeholder="PO terms or notes..."
                      value={formData.notes}
                      onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary">Submit Purchase Order</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PurchaseOrderListPage;
