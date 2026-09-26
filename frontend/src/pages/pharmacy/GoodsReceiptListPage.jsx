import React, { useState, useEffect } from 'react';
import pharmacyService from '../../services/pharmacyService';

const GoodsReceiptListPage = () => {
  const [goodsReceipts, setGoodsReceipts] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [medicines, setMedicines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);

  const [formData, setFormData] = useState({
    supplierId: '',
    invoiceNumber: '',
    invoiceDate: '',
    remarks: '',
    items: [
      { medicineId: '', batchNumber: '', quantityReceived: 100, purchaseRate: 10.00, mrp: 20.00, expiryDate: '' }
    ]
  });

  useEffect(() => {
    fetchGoodsReceipts();
    fetchSuppliersAndMedicines();
  }, []);

  const fetchGoodsReceipts = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getGoodsReceipts(0, 100);
      setGoodsReceipts(res.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load Goods Receipts');
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

  const handleAddItem = () => {
    setFormData({
      ...formData,
      items: [...formData.items, { medicineId: '', batchNumber: '', quantityReceived: 100, purchaseRate: 10.00, mrp: 20.00, expiryDate: '' }]
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
        invoiceNumber: formData.invoiceNumber,
        invoiceDate: formData.invoiceDate || null,
        remarks: formData.remarks,
        items: formData.items.map(item => ({
          medicineId: Number(item.medicineId),
          batchNumber: item.batchNumber,
          quantityReceived: Number(item.quantityReceived),
          purchaseRate: Number(item.purchaseRate),
          mrp: Number(item.mrp),
          expiryDate: item.expiryDate
        }))
      };
      await pharmacyService.createGoodsReceipt(payload);
      setShowModal(false);
      fetchGoodsReceipts();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to receive stock via GRN');
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Goods Receipt Notes (GRN)</h2>
          <p className="text-muted small">Record stock arrivals, verify batch numbers & update active stock inventory</p>
        </div>
        <button className="btn btn-success shadow-sm" onClick={() => setShowModal(true)}>
          <i className="bi bi-box-seam me-1"></i> Receive Stock (GRN)
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
                    <th>GRN ID</th>
                    <th>Supplier</th>
                    <th>Receipt Date</th>
                    <th>Invoice No.</th>
                    <th>Invoice Date</th>
                    <th>Items Received</th>
                    <th>Received By</th>
                  </tr>
                </thead>
                <tbody>
                  {goodsReceipts.length > 0 ? (
                    goodsReceipts.map((gr) => (
                      <tr key={gr.id}>
                        <td className="fw-bold text-success">{gr.goodsReceiptId}</td>
                        <td className="fw-bold">{gr.supplierName}</td>
                        <td>{gr.receiptDate}</td>
                        <td><span className="badge bg-light text-dark border">{gr.invoiceNumber || 'N/A'}</span></td>
                        <td>{gr.invoiceDate || 'N/A'}</td>
                        <td><span className="badge bg-primary">{gr.items ? gr.items.length : 0} items</span></td>
                        <td>{gr.receivedBy || 'System'}</td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="7" className="text-center py-4 text-muted">No goods receipt records found.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Receive Stock (GRN) Modal */}
      {showModal && (
        <div className="modal d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-xl modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <div className="modal-header">
                <h5 className="modal-title fw-bold">Goods Receipt / Stock Receiving (GRN)</h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  <div className="row g-3 mb-3">
                    <div className="col-md-4">
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

                    <div className="col-md-4">
                      <label className="form-label fw-bold">Invoice Number</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. INV-2026-9912"
                        value={formData.invoiceNumber}
                        onChange={(e) => setFormData({ ...formData, invoiceNumber: e.target.value })}
                      />
                    </div>

                    <div className="col-md-4">
                      <label className="form-label fw-bold">Invoice Date</label>
                      <input
                        type="date"
                        className="form-control"
                        value={formData.invoiceDate}
                        onChange={(e) => setFormData({ ...formData, invoiceDate: e.target.value })}
                      />
                    </div>
                  </div>

                  <h6 className="fw-bold border-bottom pb-2 mt-4">Received Stock Items & Batch Information</h6>
                  {formData.items.map((item, idx) => (
                    <div key={idx} className="row g-2 mb-2 align-items-center bg-light p-2 rounded">
                      <div className="col-md-3">
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
                          type="text"
                          className="form-control form-control-sm"
                          placeholder="Batch No. *"
                          required
                          value={item.batchNumber}
                          onChange={(e) => handleItemChange(idx, 'batchNumber', e.target.value)}
                        />
                      </div>
                      <div className="col-md-2">
                        <input
                          type="number"
                          className="form-control form-control-sm"
                          placeholder="Qty Received *"
                          required
                          min="1"
                          value={item.quantityReceived}
                          onChange={(e) => handleItemChange(idx, 'quantityReceived', e.target.value)}
                        />
                      </div>
                      <div className="col-md-2">
                        <input
                          type="number"
                          step="0.01"
                          className="form-control form-control-sm"
                          placeholder="Purchase Rate ₹ *"
                          required
                          value={item.purchaseRate}
                          onChange={(e) => handleItemChange(idx, 'purchaseRate', e.target.value)}
                        />
                      </div>
                      <div className="col-md-1">
                        <input
                          type="number"
                          step="0.01"
                          className="form-control form-control-sm"
                          placeholder="MRP ₹"
                          value={item.mrp}
                          onChange={(e) => handleItemChange(idx, 'mrp', e.target.value)}
                        />
                      </div>
                      <div className="col-md-2">
                        <input
                          type="date"
                          className="form-control form-control-sm"
                          required
                          value={item.expiryDate}
                          onChange={(e) => handleItemChange(idx, 'expiryDate', e.target.value)}
                        />
                      </div>
                    </div>
                  ))}
                  <button type="button" className="btn btn-sm btn-outline-success mt-2" onClick={handleAddItem}>
                    + Add Item Row
                  </button>

                  <div className="mt-3">
                    <label className="form-label fw-bold">Remarks</label>
                    <textarea
                      className="form-control"
                      rows="2"
                      placeholder="GRN verification remarks..."
                      value={formData.remarks}
                      onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-success">Confirm GRN & Update Inventory</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default GoodsReceiptListPage;
