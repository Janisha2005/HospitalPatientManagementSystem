import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import wardService from '../../services/wardService';
import { departmentService } from '../../services/departmentService';

const WardFormPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [formData, setFormData] = useState({
    wardCode: '',
    wardName: '',
    wardType: 'GENERAL',
    departmentId: '',
    floor: '',
    building: '',
    genderPolicy: 'MIXED',
    capacity: 10,
    isActive: true
  });

  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDepartments();
    if (isEdit) {
      fetchWard();
    }
  }, [id]);

  const fetchDepartments = async () => {
    try {
      const res = await departmentService.getAllActiveDepartments();
      const list = res.data?.data || res.data || [];
      setDepartments(list);
      if (!isEdit && list.length > 0) {
        setFormData(prev => ({ ...prev, departmentId: list[0].id }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const fetchWard = async () => {
    setLoading(true);
    try {
      const res = await wardService.getWardById(id);
      const w = res.data;
      setFormData({
        wardCode: w.wardCode,
        wardName: w.wardName,
        wardType: w.wardType,
        departmentId: w.departmentId,
        floor: w.floor || '',
        building: w.building || '',
        genderPolicy: w.genderPolicy,
        capacity: w.capacity,
        isActive: w.isActive
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load ward');
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
    setError('');
    setLoading(true);
    try {
      if (isEdit) {
        await wardService.updateWard(id, formData);
      } else {
        await wardService.createWard(formData);
      }
      navigate('/wards');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save ward');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">{isEdit ? 'Edit Ward' : 'Create New Ward'}</h2>
          <p className="text-muted small">Configure hospital ward details and capacity</p>
        </div>
        <Link to="/wards" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to Wards
        </Link>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label fw-semibold">Ward Code *</label>
                <input
                  type="text"
                  name="wardCode"
                  className="form-control"
                  placeholder="e.g. WARD-GEN-001"
                  value={formData.wardCode}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Ward Name *</label>
                <input
                  type="text"
                  name="wardName"
                  className="form-control"
                  placeholder="e.g. General Medical Ward"
                  value={formData.wardName}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Ward Type *</label>
                <select
                  name="wardType"
                  className="form-select"
                  value={formData.wardType}
                  onChange={handleChange}
                  required
                >
                  <option value="GENERAL">GENERAL</option>
                  <option value="SEMI_PRIVATE">SEMI_PRIVATE</option>
                  <option value="PRIVATE">PRIVATE</option>
                  <option value="ICU">ICU</option>
                  <option value="ICCU">ICCU</option>
                  <option value="NICU">NICU</option>
                  <option value="PICU">PICU</option>
                  <option value="EMERGENCY">EMERGENCY</option>
                  <option value="ISOLATION">ISOLATION</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Department *</label>
                <select
                  name="departmentId"
                  className="form-select"
                  value={formData.departmentId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select Department</option>
                  {departments.map(d => (
                    <option key={d.id} value={d.id}>{d.departmentName}</option>
                  ))}
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Building</label>
                <input
                  type="text"
                  name="building"
                  className="form-control"
                  placeholder="e.g. Block A"
                  value={formData.building}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Floor</label>
                <input
                  type="text"
                  name="floor"
                  className="form-control"
                  placeholder="e.g. 2nd Floor"
                  value={formData.floor}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Gender Policy *</label>
                <select
                  name="genderPolicy"
                  className="form-select"
                  value={formData.genderPolicy}
                  onChange={handleChange}
                  required
                >
                  <option value="MIXED">MIXED</option>
                  <option value="MALE">MALE</option>
                  <option value="FEMALE">FEMALE</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Total Capacity (Beds) *</label>
                <input
                  type="number"
                  name="capacity"
                  className="form-control"
                  min="1"
                  value={formData.capacity}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="col-md-6 d-flex align-items-center pt-4">
                <div className="form-check form-switch">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id="isActive"
                    name="isActive"
                    checked={formData.isActive}
                    onChange={handleChange}
                  />
                  <label className="form-check-label fw-semibold" htmlFor="isActive">
                    Is Ward Active?
                  </label>
                </div>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <Link to="/wards" className="btn btn-light me-2">Cancel</Link>
              <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                {loading ? 'Saving...' : (isEdit ? 'Update Ward' : 'Create Ward')}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default WardFormPage;
