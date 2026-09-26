import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import dischargeService from '../../services/dischargeService';

const DischargePlanningPage = () => {
  const { id } = useParams(); // admissionId
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    plannedDischargeDate: new Date().toISOString().split('T')[0],
    dischargeCondition: 'Stable',
    followUpRequired: true,
    followUpDate: '',
    followUpInstructions: '',
    homeCareInstructions: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchPlan();
  }, [id]);

  const fetchPlan = async () => {
    try {
      const res = await dischargeService.getDischargePlan(id);
      if (res.data) {
        setFormData({
          plannedDischargeDate: res.data.plannedDischargeDate || '',
          dischargeCondition: res.data.dischargeCondition || 'Stable',
          followUpRequired: res.data.followUpRequired !== undefined ? res.data.followUpRequired : true,
          followUpDate: res.data.followUpDate || '',
          followUpInstructions: res.data.followUpInstructions || '',
          homeCareInstructions: res.data.homeCareInstructions || ''
        });
      }
    } catch (err) {
      // Plan might not exist yet, clean default
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
      await dischargeService.createDischargePlan(id, formData);
      navigate(`/ipd/admissions/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save discharge plan');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Inpatient Discharge Planning</h2>
          <p className="text-muted small">Prepare expected discharge date, condition, and home care instructions</p>
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
                <label className="form-label fw-semibold">Planned Discharge Date *</label>
                <input
                  type="date"
                  name="plannedDischargeDate"
                  className="form-control"
                  value={formData.plannedDischargeDate}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Expected Condition at Discharge</label>
                <input
                  type="text"
                  name="dischargeCondition"
                  className="form-control"
                  placeholder="e.g. Hemodynamically Stable, Asymptomatic"
                  value={formData.dischargeCondition}
                  onChange={handleChange}
                />
              </div>

              <div className="col-md-6">
                <div className="form-check form-switch pt-4">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id="followUpRequired"
                    name="followUpRequired"
                    checked={formData.followUpRequired}
                    onChange={handleChange}
                  />
                  <label className="form-check-label fw-semibold" htmlFor="followUpRequired">
                    Follow-up Consultation Required?
                  </label>
                </div>
              </div>

              {formData.followUpRequired && (
                <div className="col-md-6">
                  <label className="form-label fw-semibold">Follow-Up Date</label>
                  <input
                    type="date"
                    name="followUpDate"
                    className="form-control"
                    value={formData.followUpDate}
                    onChange={handleChange}
                  />
                </div>
              )}

              <div className="col-12">
                <label className="form-label fw-semibold">Follow-Up Instructions</label>
                <textarea
                  name="followUpInstructions"
                  className="form-control"
                  rows="3"
                  placeholder="Specialist OPD follow-up instructions, blood tests to repeat..."
                  value={formData.followUpInstructions}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Home Care Instructions</label>
                <textarea
                  name="homeCareInstructions"
                  className="form-control"
                  rows="3"
                  placeholder="Wound care, activity restrictions, dietary advice, warning signs to return to emergency..."
                  value={formData.homeCareInstructions}
                  onChange={handleChange}
                ></textarea>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                {loading ? 'Saving...' : 'Save Discharge Plan'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default DischargePlanningPage;
