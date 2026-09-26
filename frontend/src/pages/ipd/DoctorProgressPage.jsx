import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';

const DoctorProgressPage = () => {
  const { id } = useParams(); // admissionId
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    clinicalAssessment: '',
    progressSummary: '',
    diagnosisUpdate: '',
    treatmentUpdate: '',
    followUpPlan: ''
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
      await api.post(`/ipd/admissions/${id}/progress-notes`, formData);
      navigate(`/ipd/admissions/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record doctor progress note');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Doctor Inpatient Daily Progress Note</h2>
          <p className="text-muted small">Record clinician daily round assessments, diagnosis updates, and plan adjustments</p>
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
              <div className="col-12">
                <label className="form-label fw-semibold">Clinical Assessment</label>
                <textarea
                  name="clinicalAssessment"
                  className="form-control"
                  rows="2"
                  placeholder="Daily round physical examination & symptom assessment..."
                  value={formData.clinicalAssessment}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Progress Summary *</label>
                <textarea
                  name="progressSummary"
                  className="form-control"
                  rows="3"
                  placeholder="Summary of patient status over the past 24 hours..."
                  value={formData.progressSummary}
                  onChange={handleChange}
                  required
                ></textarea>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Diagnosis Update</label>
                <textarea
                  name="diagnosisUpdate"
                  className="form-control"
                  rows="2"
                  placeholder="Updated provisional or final clinical diagnosis..."
                  value={formData.diagnosisUpdate}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Treatment & Medication Update</label>
                <textarea
                  name="treatmentUpdate"
                  className="form-control"
                  rows="2"
                  placeholder="Medication dosage adjustments, IV fluid orders, lab investigations ordered..."
                  value={formData.treatmentUpdate}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Follow-Up & Discharge Plan</label>
                <textarea
                  name="followUpPlan"
                  className="form-control"
                  rows="2"
                  placeholder="Plan for next 24-48 hours, upcoming diagnostic scans, expected discharge window..."
                  value={formData.followUpPlan}
                  onChange={handleChange}
                ></textarea>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                {loading ? 'Saving...' : 'Save Doctor Progress Note'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default DoctorProgressPage;
