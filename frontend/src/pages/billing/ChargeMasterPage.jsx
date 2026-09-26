import React, { useState, useEffect } from 'react';
import billingService from '../../services/billingService';

const ChargeMasterPage = () => {
  const [charges, setCharges] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [currentId, setCurrentId] = useState(null);

  const [formData, setFormData] = useState({
    chargeName: '',
    chargeCategory: 'CONSULTATION',
    description: '',
    unit: 'unit',
    baseRate: '',
    taxPercentage: '0.00',
    isActive: true
  });

  const categories = [
    'CONSULTATION', 'LABORATORY', 'RADIOLOGY', 'PHARMACY',
    'BED', 'ROOM', 'PROCEDURE', 'NURSING', 'REGISTRATION', 'EMERGENCY', 'OTHER'
  ];

  useEffect(() => {
    fetchCharges();
  }, [searchTerm]);

  const fetchCharges = async () => {
    try {
      setLoading(true);
      const res = await billingService.getAllCharges({ query: searchTerm, page: 0, size: 50 });
      if (res.data && res.data.success) {
        setCharges(res.data.data.content || []);
      }
    } catch (err) {
      setError('Failed to fetch charge master entries.');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setEditMode(false);
    setFormData({
      chargeName: '',
      chargeCategory: 'CONSULTATION',
      description: '',
      unit: 'unit',
      baseRate: '',
      taxPercentage: '0.00',
      isActive: true
    });
    setShowModal(true);
  };

  const handleOpenEditModal = (charge) => {
    setEditMode(true);
    setCurrentId(charge.id);
    setFormData({
      chargeName: charge.chargeName,
      chargeCategory: charge.chargeCategory,
      description: charge.description || '',
      unit: charge.unit || 'unit',
      baseRate: charge.baseRate,
      taxPercentage: charge.taxPercentage || '0.00',
      isActive: charge.isActive
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editMode) {
        await billingService.updateCharge(currentId, formData);
      } else {
        await billingService.createCharge(formData);
      }
      setShowModal(false);
      fetchCharges();
    } catch (err) {
      alert(err.response?.data?.message || 'Error saving charge master entry.');
    }
  };

  const toggleStatus = async (id, currentStatus) => {
    try {
      await billingService.updateChargeStatus(id, !currentStatus);
      fetchCharges();
    } catch (err) {
      alert('Failed to update status.');
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
            <i className="bi bi-list-columns-reverse text-primary me-2"></i>Charge Master Directory
          </h2>
          <p className="text-muted small mb-0">Configurable hospital charge catalog, rates, and tax parameters</p>
        </div>
        <button className="btn btn-primary rounded-pill px-3" onClick={handleOpenCreateModal}>
          <i className="bi bi-plus-lg me-1"></i>Add New Charge Rate
        </button>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-4 bg-white mb-4">
        <div className="card-body p-3">
          <div className="row g-3">
            <div className="col-12 col-md-6 col-lg-4">
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0 text-muted">
                  <i className="bi bi-search"></i>
                </span>
                <input
                  type="text"
                  className="form-control border-start-0 ps-0"
                  placeholder="Search charge name or code..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
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
                  <th className="ps-4">Code</th>
                  <th>Charge Name</th>
                  <th>Category</th>
                  <th>Unit</th>
                  <th>Base Rate</th>
                  <th>GST %</th>
                  <th>Status</th>
                  <th className="text-end pe-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-muted">Loading charge master entries...</td>
                  </tr>
                ) : charges.length > 0 ? (
                  charges.map((c) => (
                    <tr key={c.id}>
                      <td className="ps-4 fw-semibold text-primary">{c.chargeCode}</td>
                      <td>
                        <div className="fw-medium text-dark">{c.chargeName}</div>
                        <div className="small text-muted">{c.description}</div>
                      </td>
                      <td><span className="badge bg-info bg-opacity-10 text-info border">{c.chargeCategory}</span></td>
                      <td>{c.unit}</td>
                      <td className="fw-bold text-dark">{formatCurrency(c.baseRate)}</td>
                      <td>{c.taxPercentage || 0}%</td>
                      <td>
                        <span className={`badge ${c.isActive ? 'bg-success' : 'bg-secondary'}`}>
                          {c.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td className="text-end pe-4">
                        <button className="btn btn-sm btn-light border me-2" onClick={() => handleOpenEditModal(c)}>
                          <i className="bi bi-pencil me-1"></i>Edit
                        </button>
                        <button
                          className={`btn btn-sm ${c.isActive ? 'btn-outline-danger' : 'btn-outline-success'}`}
                          onClick={() => toggleStatus(c.id, c.isActive)}
                        >
                          {c.isActive ? 'Deactivate' : 'Activate'}
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-muted">No charge master entries found.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Modal Form */}
      {showModal && (
        <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content rounded-4 border-0 shadow">
              <div className="modal-header border-0 pb-0">
                <h5 className="modal-title fw-bold">
                  {editMode ? 'Edit Charge Entry' : 'Add New Charge Entry'}
                </h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label fw-medium">Charge Name *</label>
                    <input
                      type="text"
                      className="form-control"
                      required
                      value={formData.chargeName}
                      onChange={(e) => setFormData({ ...formData, chargeName: e.target.value })}
                    />
                  </div>
                  <div className="row g-3 mb-3">
                    <div className="col-6">
                      <label className="form-label fw-medium">Category *</label>
                      <select
                        className="form-select"
                        value={formData.chargeCategory}
                        onChange={(e) => setFormData({ ...formData, chargeCategory: e.target.value })}
                      >
                        {categories.map((cat) => (
                          <option key={cat} value={cat}>{cat}</option>
                        ))}
                      </select>
                    </div>
                    <div className="col-6">
                      <label className="form-label fw-medium">Unit *</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. per day, per test"
                        value={formData.unit}
                        onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                      />
                    </div>
                  </div>
                  <div className="row g-3 mb-3">
                    <div className="col-6">
                      <label className="form-label fw-medium">Base Rate (₹) *</label>
                      <input
                        type="number"
                        step="0.01"
                        className="form-control"
                        required
                        value={formData.baseRate}
                        onChange={(e) => setFormData({ ...formData, baseRate: e.target.value })}
                      />
                    </div>
                    <div className="col-6">
                      <label className="form-label fw-medium">GST / Tax %</label>
                      <input
                        type="number"
                        step="0.01"
                        className="form-control"
                        value={formData.taxPercentage}
                        onChange={(e) => setFormData({ ...formData, taxPercentage: e.target.value })}
                      />
                    </div>
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-medium">Description</label>
                    <textarea
                      className="form-control"
                      rows="2"
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer border-0 pt-0">
                  <button type="button" className="btn btn-light rounded-pill px-3" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary rounded-pill px-4">
                    {editMode ? 'Update Rate' : 'Save Charge'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChargeMasterPage;
