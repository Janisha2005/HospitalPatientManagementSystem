import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import ipdService from '../../services/ipdService';
import wardService from '../../services/wardService';
import bedService from '../../services/bedService';

const BedAllocationPage = () => {
  const { id } = useParams(); // admissionId
  const navigate = useNavigate();

  const [admission, setAdmission] = useState(null);
  const [wards, setWards] = useState([]);
  const [availableBeds, setAvailableBeds] = useState([]);

  const [formData, setFormData] = useState({
    wardId: '',
    bedId: '',
    allocationType: 'INITIAL',
    reason: 'Initial Bed Allocation'
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchData();
  }, [id]);

  useEffect(() => {
    if (formData.wardId) {
      fetchAvailableBeds(formData.wardId);
    }
  }, [formData.wardId]);

  const fetchData = async () => {
    try {
      const [admRes, wRes] = await Promise.all([
        ipdService.getAdmissionById(id),
        wardService.getAllWards(null, true)
      ]);
      setAdmission(admRes.data);
      setWards(wRes.data || []);
      if (admRes.data?.departmentId) {
        const filteredWards = (wRes.data || []).filter(w => w.departmentId === admRes.data.departmentId);
        if (filteredWards.length > 0) {
          setFormData(prev => ({ ...prev, wardId: filteredWards[0].id }));
        }
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load details');
    }
  };

  const fetchAvailableBeds = async (wardId) => {
    try {
      const res = await bedService.getAvailableBeds(wardId);
      setAvailableBeds(res.data || []);
      if (res.data && res.data.length > 0) {
        setFormData(prev => ({ ...prev, bedId: res.data[0].id }));
      } else {
        setFormData(prev => ({ ...prev, bedId: '' }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await ipdService.allocateBed(id, formData.wardId, formData.bedId, formData.allocationType, formData.reason);
      navigate(`/ipd/admissions/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to allocate bed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Allocate Bed & Admit Patient</h2>
          <p className="text-muted small">Select an available bed from configured hospital wards</p>
        </div>
        <Link to={`/ipd/admissions/${id}`} className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to Admission
        </Link>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      {admission && (
        <div className="card border-0 shadow-sm rounded-3 mb-4 bg-light">
          <div className="card-body p-3 d-flex justify-content-between align-items-center">
            <div>
              <span className="fw-bold text-primary font-monospace">{admission.admissionId}</span> &bull;{' '}
              <span className="fw-bold">{admission.patientName}</span> ({admission.patientCode}) &bull;{' '}
              <span>Department: {admission.departmentName}</span>
            </div>
            <span className="badge bg-primary">{admission.status}</span>
          </div>
        </div>
      )}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label fw-semibold">Target Ward *</label>
                <select
                  name="wardId"
                  className="form-select"
                  value={formData.wardId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select Ward</option>
                  {wards.map(w => (
                    <option key={w.id} value={w.id}>
                      {w.wardName} ({w.wardCode}) - {w.availableBeds} beds available
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Target Bed *</label>
                <select
                  name="bedId"
                  className="form-select"
                  value={formData.bedId}
                  onChange={handleChange}
                  required
                  disabled={!formData.wardId}
                >
                  <option value="">Select Available Bed</option>
                  {availableBeds.map(b => (
                    <option key={b.id} value={b.id}>
                      {b.bedCode} - Bed Number {b.bedNumber} ({b.bedType})
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Allocation Type *</label>
                <select
                  name="allocationType"
                  className="form-select"
                  value={formData.allocationType}
                  onChange={handleChange}
                  required
                >
                  <option value="INITIAL">INITIAL</option>
                  <option value="RESERVATION">RESERVATION</option>
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Allocation Reason</label>
                <input
                  type="text"
                  name="reason"
                  className="form-control"
                  value={formData.reason}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <button type="submit" className="btn btn-primary px-4" disabled={loading || !formData.bedId}>
                {loading ? 'Allocating...' : 'Allocate Bed & Admit'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default BedAllocationPage;
