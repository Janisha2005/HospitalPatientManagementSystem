import React, { useState, useEffect } from 'react';
import pharmacyService from '../../services/pharmacyService';

const MedicineCategoryPage = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState(null);

  const [formData, setFormData] = useState({
    categoryCode: '',
    categoryName: '',
    description: '',
    isActive: true
  });

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getCategories(0, 100);
      setCategories(res.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (cat = null) => {
    if (cat) {
      setEditId(cat.id);
      setFormData({
        categoryCode: cat.categoryCode,
        categoryName: cat.categoryName,
        description: cat.description || '',
        isActive: cat.isActive !== undefined ? cat.isActive : true
      });
    } else {
      setEditId(null);
      setFormData({
        categoryCode: '',
        categoryName: '',
        description: '',
        isActive: true
      });
    }
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editId) {
        await pharmacyService.updateCategory(editId, formData);
      } else {
        await pharmacyService.createCategory(formData);
      }
      setShowModal(false);
      fetchCategories();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save category');
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Medicine Categories</h2>
          <p className="text-muted small">Manage drug classification & active categories</p>
        </div>
        <button className="btn btn-primary shadow-sm" onClick={() => handleOpenModal()}>
          <i className="bi bi-plus-lg me-1"></i> Add Category
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
                    <th>Code</th>
                    <th>Category Name</th>
                    <th>Description</th>
                    <th>Status</th>
                    <th className="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {categories.map((c) => (
                    <tr key={c.id}>
                      <td className="fw-bold text-primary">{c.categoryCode}</td>
                      <td className="fw-bold">{c.categoryName}</td>
                      <td>{c.description || 'N/A'}</td>
                      <td>
                        <span className={`badge ${c.isActive ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'}`}>
                          {c.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td className="text-end">
                        <button className="btn btn-sm btn-outline-primary me-1" onClick={() => handleOpenModal(c)}>
                          <i className="bi bi-pencil"></i> Edit
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Simple Modal overlay */}
      {showModal && (
        <div className="modal d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <div className="modal-header">
                <h5 className="modal-title fw-bold">{editId ? 'Edit Category' : 'Add Category'}</h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label fw-bold">Category Code *</label>
                    <input
                      type="text"
                      className="form-control"
                      required
                      disabled={Boolean(editId)}
                      placeholder="e.g. CAT-ANALGESICS"
                      value={formData.categoryCode}
                      onChange={(e) => setFormData({ ...formData, categoryCode: e.target.value })}
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Category Name *</label>
                    <input
                      type="text"
                      className="form-control"
                      required
                      placeholder="e.g. Analgesics & Antipyretics"
                      value={formData.categoryName}
                      onChange={(e) => setFormData({ ...formData, categoryName: e.target.value })}
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Description</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      placeholder="Category description..."
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    ></textarea>
                  </div>
                  <div className="form-check form-switch mb-3">
                    <input
                      type="checkbox"
                      className="form-check-input"
                      id="catIsActive"
                      checked={formData.isActive}
                      onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                    />
                    <label className="form-check-label fw-bold" htmlFor="catIsActive">Active Category</label>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary">{editId ? 'Update' : 'Save'}</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MedicineCategoryPage;
