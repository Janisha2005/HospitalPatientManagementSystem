import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import dischargeService from '../../services/dischargeService';

const DischargeSummaryPage = () => {
  const { id } = useParams(); // admissionId
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    admissionSummary: '',
    clinicalCourse: '',
    finalDiagnosis: '',
    proceduresSummary: '',
    investigationSummary: '',
    treatmentSummary: '',
    medicationSummary: '',
    conditionAtDischarge: 'Stable',
    followUpInstructions: '',
    dischargeDate: new Date().toISOString().split('T')[0]
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchSummary();
  }, [id]);

  const fetchSummary = async () => {
    try {
      const res = await dischargeService.getDischargeSummary(id);
      if (res.data) {
        setFormData({
          admissionSummary: res.data.admissionSummary || '',
          clinicalCourse: res.data.clinicalCourse || '',
          finalDiagnosis: res.data.finalDiagnosis || '',
          proceduresSummary: res.data.proceduresSummary || '',
          investigationSummary: res.data.investigationSummary || '',
          treatmentSummary: res.data.treatmentSummary || '',
          medicationSummary: res.data.medicationSummary || '',
          conditionAtDischarge: res.data.conditionAtDischarge || 'Stable',
          followUpInstructions: res.data.followUpInstructions || '',
          dischargeDate: res.data.dischargeDate || new Date().toISOString().split('T')[0]
        });
      }
    } catch (err) {
      // Summary might not exist yet
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
      await dischargeService.createDischargeSummary(id, formData);
      navigate(`/ipd/admissions/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save discharge summary');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Formal Discharge Summary Document</h2>
          <p className="text-muted small">Comprehensive inpatient medical summary required for clinical discharge approval</p>
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
              <div className="col-md-6">
                <label className="form-label fw-semibold">Discharge Date *</label>
                <input
                  type="date"
                  name="dischargeDate"
                  className="form-control"
                  value={formData.dischargeDate}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Condition at Discharge</label>
                <input
                  type="text"
                  name="conditionAtDischarge"
                  className="form-control"
                  placeholder="e.g. Asymptomatic, Hemodynamically Stable"
                  value={formData.conditionAtDischarge}
                  onChange={handleChange}
                />
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Final Clinical Diagnosis</label>
                <textarea
                  name="finalDiagnosis"
                  className="form-control"
                  rows="2"
                  placeholder="Final ICD-10 diagnosis confirmed during IPD stay..."
                  value={formData.finalDiagnosis}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Admission Summary & Chief Complaint</label>
                <textarea
                  name="admissionSummary"
                  className="form-control"
                  rows="3"
                  placeholder="Reason for admission, initial presenting symptoms..."
                  value={formData.admissionSummary}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Hospital Clinical Course</label>
                <textarea
                  name="clinicalCourse"
                  className="form-control"
                  rows="4"
                  placeholder="Day-by-day clinical progress, response to treatment, consultations..."
                  value={formData.clinicalCourse}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Procedures Performed Summary</label>
                <textarea
                  name="proceduresSummary"
                  className="form-control"
                  rows="3"
                  placeholder="Surgical / interventional procedures performed during stay..."
                  value={formData.proceduresSummary}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Investigations & Lab/Rad Results Summary</label>
                <textarea
                  name="investigationSummary"
                  className="form-control"
                  rows="3"
                  placeholder="Key laboratory & radiology findings..."
                  value={formData.investigationSummary}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Treatment & Interventions Administered</label>
                <textarea
                  name="treatmentSummary"
                  className="form-control"
                  rows="3"
                  placeholder="Inpatient treatment regimen administered..."
                  value={formData.treatmentSummary}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Discharge Medications Prescribed</label>
                <textarea
                  name="medicationSummary"
                  className="form-control"
                  rows="3"
                  placeholder="Medications to continue at home with dosages & frequencies..."
                  value={formData.medicationSummary}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Follow-Up Instructions & Advice</label>
                <textarea
                  name="followUpInstructions"
                  className="form-control"
                  rows="3"
                  placeholder="Follow-up appointment instructions, OPD visits, emergency contact..."
                  value={formData.followUpInstructions}
                  onChange={handleChange}
                ></textarea>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                {loading ? 'Saving Summary...' : 'Save Discharge Summary'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default DischargeSummaryPage;
