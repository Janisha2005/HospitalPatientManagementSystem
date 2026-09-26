import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import nursingService from '../../services/nursingService';

const InpatientVitalsPage = () => {
  const { id } = useParams(); // admissionId
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    temperature: '',
    pulse: '',
    bloodPressure: '',
    respiratoryRate: '',
    oxygenSaturation: '',
    heightCm: '',
    weightKg: '',
    painScore: '',
    notes: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await nursingService.recordVitals(id, formData);
      navigate(`/ipd/admissions/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record vitals');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Record Inpatient Vital Signs</h2>
          <p className="text-muted small">Capture clinical vitals timestamped into historical record</p>
        </div>
        <Link to={`/ipd/admissions/${id}`} className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to Patient Chart
        </Link>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-4">
                <label className="form-label fw-semibold">Temperature (°C)</label>
                <input
                  type="number"
                  step="0.1"
                  name="temperature"
                  className="form-control"
                  placeholder="e.g. 37.0"
                  value={formData.temperature}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Blood Pressure (mmHg)</label>
                <input
                  type="text"
                  name="bloodPressure"
                  className="form-control"
                  placeholder="e.g. 120/80"
                  value={formData.bloodPressure}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Pulse Rate (bpm)</label>
                <input
                  type="number"
                  name="pulse"
                  className="form-control"
                  placeholder="e.g. 72"
                  value={formData.pulse}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Oxygen Saturation - SpO2 (%)</label>
                <input
                  type="number"
                  name="oxygenSaturation"
                  className="form-control"
                  placeholder="e.g. 99"
                  value={formData.oxygenSaturation}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Respiratory Rate (breaths/min)</label>
                <input
                  type="number"
                  name="respiratoryRate"
                  className="form-control"
                  placeholder="e.g. 16"
                  value={formData.respiratoryRate}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Pain Score (0 - 10)</label>
                <input
                  type="number"
                  min="0"
                  max="10"
                  name="painScore"
                  className="form-control"
                  placeholder="e.g. 2"
                  value={formData.painScore}
                  onChange={handleChange}
                />
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Nursing Notes / Observations</label>
                <textarea
                  name="notes"
                  className="form-control"
                  rows="3"
                  placeholder="Observations regarding patient comfort, rhythm, oxygen support..."
                  value={formData.notes}
                  onChange={handleChange}
                ></textarea>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                {loading ? 'Saving...' : 'Save Vital Signs'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default InpatientVitalsPage;
