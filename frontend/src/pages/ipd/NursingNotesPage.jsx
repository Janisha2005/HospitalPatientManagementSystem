import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import nursingService from '../../services/nursingService';

const NursingNotesPage = () => {
  const { id } = useParams(); // admissionId
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    noteType: 'ROUTINE_NOTE',
    noteText: ''
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
      await nursingService.createNursingNote(id, formData);
      navigate(`/ipd/admissions/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create nursing note');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Add Nursing Note</h2>
          <p className="text-muted small">Record shift handovers, routine ward observations, and nursing care notes</p>
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
                <label className="form-label fw-semibold">Note Type *</label>
                <select
                  name="noteType"
                  className="form-select"
                  value={formData.noteType}
                  onChange={handleChange}
                  required
                >
                  <option value="INITIAL_ASSESSMENT">INITIAL_ASSESSMENT</option>
                  <option value="ROUTINE_NOTE">ROUTINE_NOTE</option>
                  <option value="SHIFT_HANDOVER">SHIFT_HANDOVER</option>
                  <option value="CARE_NOTE">CARE_NOTE</option>
                  <option value="OBSERVATION">OBSERVATION</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Note Text *</label>
                <textarea
                  name="noteText"
                  className="form-control"
                  rows="5"
                  placeholder="Detailed nursing care observations, medication administration, IV line checks..."
                  value={formData.noteText}
                  onChange={handleChange}
                  required
                ></textarea>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                {loading ? 'Saving...' : 'Save Nursing Note'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default NursingNotesPage;
