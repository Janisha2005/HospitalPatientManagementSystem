import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import pharmacyService from '../../services/pharmacyService';

const INDIAN_STATES = [
  "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
  "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh",
  "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
  "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh",
  "Uttarakhand", "West Bengal", "Delhi", "Jammu and Kashmir", "Puducherry"
];

const SupplierFormPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [formData, setFormData] = useState({
    supplierName: '',
    contactPerson: '',
    phone: '',
    email: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    district: '',
    state: 'Tamil Nadu',
    pincode: '',
    gstNumber: '',
    drugLicenseNumber: '',
    paymentTerms: 'Net 30 Days',
    isActive: true
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isEdit) {
      fetchSupplier();
    }
  }, [id]);

  const fetchSupplier = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getSupplierById(id);
      const s = res.data;
      setFormData({
        supplierName: s.supplierName || '',
        contactPerson: s.contactPerson || '',
        phone: s.phone || '',
        email: s.email || '',
        addressLine1: s.addressLine1 || '',
        addressLine2: s.addressLine2 || '',
        city: s.city || '',
        district: s.district || '',
        state: s.state || 'Tamil Nadu',
        pincode: s.pincode || '',
        gstNumber: s.gstNumber || '',
        drugLicenseNumber: s.drugLicenseNumber || '',
        paymentTerms: s.paymentTerms || 'Net 30 Days',
        isActive: s.isActive !== undefined ? s.isActive : true
      });
    } catch (err) {
      setError('Failed to load supplier details');
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
      if (isEdit) {
        await pharmacyService.updateSupplier(id, formData);
      } else {
        await pharmacyService.createSupplier(formData);
      }
      navigate('/pharmacy/suppliers');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save supplier details');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-4" style={{ maxWidth: '800px' }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">{isEdit ? 'Edit Supplier' : 'Register New Supplier'}</h2>
          <p className="text-muted small">Configure supplier contact, drug license & GST registration</p>
        </div>
        <Link to="/pharmacy/suppliers" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to Suppliers
        </Link>
      </div>

      {error && <div className="alert alert-danger mb-4">{error}</div>}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label fw-bold">Supplier Name *</label>
                <input
                  type="text"
                  name="supplierName"
                  className="form-control"
                  required
                  placeholder="e.g. MedPlus Supply Solutions Pvt Ltd"
                  value={formData.supplierName}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Contact Person</label>
                <input
                  type="text"
                  name="contactPerson"
                  className="form-control"
                  placeholder="e.g. Suresh Verma"
                  value={formData.contactPerson}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Phone (10 Digits) *</label>
                <input
                  type="text"
                  name="phone"
                  className="form-control"
                  required
                  placeholder="e.g. +919876543210"
                  value={formData.phone}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Email Address</label>
                <input
                  type="email"
                  name="email"
                  className="form-control"
                  placeholder="e.g. sales@medplus.in"
                  value={formData.email}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Address Line 1</label>
                <input
                  type="text"
                  name="addressLine1"
                  className="form-control"
                  placeholder="Street / Building"
                  value={formData.addressLine1}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Address Line 2</label>
                <input
                  type="text"
                  name="addressLine2"
                  className="form-control"
                  placeholder="Area / Landmark"
                  value={formData.addressLine2}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-bold">City</label>
                <input
                  type="text"
                  name="city"
                  className="form-control"
                  placeholder="e.g. Chennai / Bengaluru"
                  value={formData.city}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-bold">State *</label>
                <select
                  name="state"
                  className="form-select"
                  value={formData.state}
                  onChange={handleChange}
                >
                  {INDIAN_STATES.map(s => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label fw-bold">PIN Code (6 Digits)</label>
                <input
                  type="text"
                  name="pincode"
                  className="form-control"
                  placeholder="e.g. 600001"
                  value={formData.pincode}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">GST Number</label>
                <input
                  type="text"
                  name="gstNumber"
                  className="form-control"
                  placeholder="e.g. 33AABCC5678J1Z2"
                  value={formData.gstNumber}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Drug License Number</label>
                <input
                  type="text"
                  name="drugLicenseNumber"
                  className="form-control"
                  placeholder="e.g. TN-CHN-2021-DL-4321"
                  value={formData.drugLicenseNumber}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-bold">Payment Terms</label>
                <select
                  name="paymentTerms"
                  className="form-select"
                  value={formData.paymentTerms}
                  onChange={handleChange}
                >
                  <option value="Net 15 Days">Net 15 Days</option>
                  <option value="Net 30 Days">Net 30 Days</option>
                  <option value="Net 45 Days">Net 45 Days</option>
                  <option value="Advance Payment">Advance Payment</option>
                  <option value="Cash on Delivery">Cash on Delivery</option>
                </select>
              </div>

              <div className="col-md-6 mt-4">
                <div className="form-check form-switch mt-3">
                  <input
                    type="checkbox"
                    name="isActive"
                    className="form-check-input"
                    id="supplierIsActive"
                    checked={formData.isActive}
                    onChange={handleChange}
                  />
                  <label className="form-check-label fw-bold" htmlFor="supplierIsActive">
                    Active Supplier Status
                  </label>
                </div>
              </div>

              <div className="col-12 mt-4 d-flex justify-content-end gap-2">
                <Link to="/pharmacy/suppliers" className="btn btn-secondary">Cancel</Link>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Saving...' : (isEdit ? 'Update Supplier' : 'Save Supplier')}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SupplierFormPage;
