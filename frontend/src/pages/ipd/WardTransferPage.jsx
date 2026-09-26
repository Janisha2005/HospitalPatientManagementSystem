import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import ipdService from '../../services/ipdService';
import wardService from '../../services/wardService';
import bedService from '../../services/bedService';

const WardTransferPage = () => {
  const { id } = useParams(); // admissionId
  const navigate = useNavigate();

  const [admission, setAdmission] = useState(null);
  const [wards, setWards] = useState([]);
  const [availableBeds, setAvailableBeds] = useState([]);

  const [formData, setFormData] = useState({
    toWardId: '',
    toBedId: '',
    reason: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchData();
  }, [id]);

  useEffect(() => {
    if (formData.toWardId) {
      fetchAvailableBeds(formData.toWardId);
    }
  }, [formData.toWardId]);

  const fetchData = async () => {
    try {
      const [admRes, wRes] = await Promise.all([
        ipdService.getAdmissionById(id),
        wardService.getAllWards(null, true)
      ]);
      setAdmission(admRes.data);
      setWards(wRes.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load details');
    }
  };

  const fetchAvailableBeds = async (wardId) => {
    try {
      const res = await bedService.getAvailableBeds(wardId);
      setAvailableBeds(res.data || []);
      if (res.data && res.data.length > 0) {
        setFormData(prev => ({ ...prev, toBedId: res.data[0].id }));
      } else {
        setFormData(prev => ({ ...prev, toBedId: '' }));
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
      await ipdService.transferPatient(id, formData.toWardId, formData.toBedId, formData.reason);
      navigate(`/ipd/admissions/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to transfer patient');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Ward & Bed Transfer</h2>
          <p className="text-muted small">Transfer patient transactionally from current bed to target ward/bed</p>
        </div>
        <Link to={`/ipd/admissions/${id}`} className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to Admission
        </Link>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      {admission && (
        <div className="card border-0 shadow-sm rounded-3 mb-4 bg-light">
          <div className="card-body p-3">
            <div className="row align-items-center">
              <div className="col-md-6">
                <span className="fw-bold text-primary font-monospace">{admission.admissionId}</span> &bull;{' '}
                <span className="fw-bold text-dark">{admission.patientName}</span>
              </div>
              <div className="col-md-6 text-end">
                Current Location: <span className="badge bg-primary font-monospace">{admission.wardName} ({admission.bedCode})</span>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label fw-semibold">Target Destination Ward *</label>
                <select
                  name="toWardId"
                  className="form-select"
                  value={formData.toWardId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select Target Ward</option>
                  {wards.map(w => (
                    <option key={w.id} value={w.id}>
                      {w.wardName} ({w.wardCode}) - {w.availableBeds} beds available
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Target Destination Bed *</label>
                <select
                  name="toBedId"
                  className="form-select"
                  value={formData.toBedId}
                  onChange={handleChange}
                  required
                  disabled={!formData.toWardId}
                >
                  <option value="">Select Available Target Bed</option>
                  {availableBeds.map(b => (
                    <option key={b.id} value={b.id}>
                      {b.bedCode} - Bed Number {b.bedNumber} ({b.bedType})
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Transfer Reason *</label>
                <textarea
                  name="reason"
                  className="form-control"
                  rows="3"
                  placeholder="Clinical reason for ward transfer (e.g. ICU step-down, specialty ward request, isolation)..."
                  value={formData.reason}
                  onChange={handleChange}
                  required
                ></textarea>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <button type="submit" className="btn btn-primary px-4" disabled={loading || !formData.toBedId}>
                {loading ? 'Executing Transfer...' : 'Execute Ward Transfer'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default WardTransferPage;
