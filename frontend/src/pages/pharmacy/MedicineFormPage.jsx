import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import pharmacyService from '../../services/pharmacyService';

const MedicineFormPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    medicineCode: '',
    medicineName: '',
    genericName: '',
    strength: '',
    dosageForm: 'Tablet',
    manufacturer: '',
    categoryId: '',
    unit: 'Strip (10 Tab)',
    reorderLevel: 50,
    maximumStockLevel: 500,
    isPrescriptionRequired: true,
    isControlled: false,
    isActive: true
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchCategories();
    if (isEdit) {
      fetchMedicine();
    }
  }, [id]);

  const fetchCategories = async () => {
    try {
      const res = await pharmacyService.getActiveCategories();
      setCategories(res.data || []);
    } catch (err) {
      console.error('Failed to load categories', err);
    }
  };

  const fetchMedicine = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getMedicineById(id);
      const m = res.data;
      setFormData({
        medicineCode: m.medicineCode || '',
        medicineName: m.medicineName || '',
        genericName: m.genericName || '',
        strength: m.strength || '',
        dosageForm: m.dosageForm || 'Tablet',
        manufacturer: m.manufacturer || '',
        categoryId: m.category ? m.category.id : '',
        unit: m.unit || 'Strip (10 Tab)',
        reorderLevel: m.reorderLevel || 50,
        maximumStockLevel: m.maximumStockLevel || 500,
        isPrescriptionRequired: m.isPrescriptionRequired !== undefined ? m.isPrescriptionRequired : true,
        isControlled: m.isControlled !== undefined ? m.isControlled : false,
        isActive: m.isActive !== undefined ? m.isActive : true
      });
    } catch (err) {
      setError('Failed to load medicine details');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const payload = {
        ...formData,
        categoryId: formData.categoryId ? Number(formData.categoryId) : null,
        reorderLevel: Number(formData.reorderLevel),
        maximumStockLevel: Number(formData.maximumStockLevel)
      };

      if (isEdit) {
        await pharmacyService.updateMedicine(id, payload);
      } else {
        await pharmacyService.createMedicine(payload);
      }
      navigate('/pharmacy/medicines');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save medicine');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-4" style={{ maxWidth: '800px' }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">{isEdit ? 'Edit Medicine' : 'Add New Medicine'}</h2>
          <p className="text-muted small">Configure medicine details, category & stock reorder rules</p>
        </div>
        <Link to="/pharmacy/medicines" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to Catalog
        </Link>
      </div>

      {error && <div className="alert alert-danger mb-4">{error}</div>}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label fw-bold">Medicine Code *</label>
                <input
                  type="text"
                  name="medicineCode"
                  className="form-control"
                  required
                  disabled={isEdit}
                  placeholder="e.g. MED-PCM-500"
                  value={formData.medicineCode}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Brand Name *</label>
                <input
                  type="text"
                  name="medicineName"
                  className="form-control"
                  required
                  placeholder="e.g. Paracetamol 500mg"
                  value={formData.medicineName}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Generic Name</label>
                <input
                  type="text"
                  name="genericName"
                  className="form-control"
                  placeholder="e.g. Paracetamol / Acetaminophen"
                  value={formData.genericName}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Category</label>
                <select
                  name="categoryId"
                  className="form-select"
                  value={formData.categoryId}
                  onChange={handleChange}
                >
                  <option value="">-- Select Category --</option>
                  {categories.map(c => (
                    <option key={c.id} value={c.id}>{c.categoryName}</option>
                  ))}
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label fw-bold">Dosage Form</label>
                <select
                  name="dosageForm"
                  className="form-select"
                  value={formData.dosageForm}
                  onChange={handleChange}
                >
                  <option value="Tablet">Tablet</option>
                  <option value="Capsule">Capsule</option>
                  <option value="Syrup">Syrup</option>
                  <option value="Injection">Injection</option>
                  <option value="Ointment">Ointment</option>
                  <option value="Inhaler">Inhaler</option>
                  <option value="Sachet">Sachet</option>
                  <option value="Drops">Drops</option>
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label fw-bold">Strength</label>
                <input
                  type="text"
                  name="strength"
                  className="form-control"
                  placeholder="e.g. 500 mg, 5 mg/ml"
                  value={formData.strength}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-bold">Packaging Unit</label>
                <input
                  type="text"
                  name="unit"
                  className="form-control"
                  placeholder="e.g. Strip (10 Tab), Bottle (100 ml)"
                  value={formData.unit}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Manufacturer</label>
                <input
                  type="text"
                  name="manufacturer"
                  className="form-control"
                  placeholder="e.g. Cipla / Mankind / GSK"
                  value={formData.manufacturer}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-3">
                <label className="form-label fw-bold">Reorder Level *</label>
                <input
                  type="number"
                  name="reorderLevel"
                  className="form-control"
                  required
                  min="0"
                  value={formData.reorderLevel}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-3">
                <label className="form-label fw-bold">Max Stock Level *</label>
                <input
                  type="number"
                  name="maximumStockLevel"
                  className="form-control"
                  required
                  min="0"
                  value={formData.maximumStockLevel}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4 mt-4">
                <div className="form-check form-switch">
                  <input
                    type="checkbox"
                    name="isPrescriptionRequired"
                    className="form-check-input"
                    id="isPrescriptionRequired"
                    checked={formData.isPrescriptionRequired}
                    onChange={handleChange}
                  />
                  <label className="form-check-label fw-bold" htmlFor="isPrescriptionRequired">
                    Prescription Required (Rx)
                  </label>
                </div>
              </div>

              <div className="col-md-4 mt-4">
                <div className="form-check form-switch">
                  <input
                    type="checkbox"
                    name="isControlled"
                    className="form-check-input"
                    id="isControlled"
                    checked={formData.isControlled}
                    onChange={handleChange}
                  />
                  <label className="form-check-label fw-bold" htmlFor="isControlled">
                    Controlled / High Alert
                  </label>
                </div>
              </div>

              <div className="col-md-4 mt-4">
                <div className="form-check form-switch">
                  <input
                    type="checkbox"
                    name="isActive"
                    className="form-check-input"
                    id="isActive"
                    checked={formData.isActive}
                    onChange={handleChange}
                  />
                  <label className="form-check-label fw-bold" htmlFor="isActive">
                    Active Catalog Status
                  </label>
                </div>
              </div>

              <div className="col-12 mt-4 d-flex justify-content-end gap-2">
                <Link to="/pharmacy/medicines" className="btn btn-secondary">Cancel</Link>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Saving...' : (isEdit ? 'Update Medicine' : 'Save Medicine')}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default MedicineFormPage;
