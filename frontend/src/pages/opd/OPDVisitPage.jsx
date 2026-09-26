import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { opdService } from '../../services/opdService';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import StatusBadge from '../../components/StatusBadge';
import { authService } from '../../services/authService';

const OPDVisitPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [visit, setVisit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  // Vitals form
  const [vitals, setVitals] = useState({
    vitalTemperature: '',
    vitalPulse: '',
    vitalBloodPressure: '',
    vitalRespiratoryRate: '',
    vitalOxygenSaturation: '',
    heightCm: '',
    weightKg: '',
    chiefComplaint: '',
    clinicalNotes: '',
    diagnosis: '',
    treatmentPlan: ''
  });

  const isDoctor = authService.hasRole('DOCTOR');
  const isAdmin = authService.hasRole('ADMIN');

  useEffect(() => {
    fetchVisit();
  }, [id]);

  const fetchVisit = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await opdService.getVisitById(id);
      if (res.success) {
        setVisit(res.data);
        setVitals({
          vitalTemperature: res.data.vitalTemperature || '',
          vitalPulse: res.data.vitalPulse || '',
          vitalBloodPressure: res.data.vitalBloodPressure || '',
          vitalRespiratoryRate: res.data.vitalRespiratoryRate || '',
          vitalOxygenSaturation: res.data.vitalOxygenSaturation || '',
          heightCm: res.data.heightCm || '',
          weightKg: res.data.weightKg || '',
          chiefComplaint: res.data.chiefComplaint || '',
          clinicalNotes: res.data.clinicalNotes || '',
          diagnosis: res.data.diagnosis || '',
          treatmentPlan: res.data.treatmentPlan || ''
        });
      } else {
        setError(res.message);
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch OPD visit details');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveVitals = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await opdService.updateVisitDetails(id, vitals);
      if (res.success) {
        alert('OPD Visit details updated successfully');
        fetchVisit();
      } else {
        alert(res.message);
      }
    } catch (err) {
      alert(err.message || 'Failed to update OPD visit details');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="py-5"><LoadingSpinner /></div>;
  if (error) return <div className="container py-4"><ErrorMessage message={error} /></div>;
  if (!visit) return null;

  return (
    <div className="container py-3" style={{ maxWidth: '950px' }}>
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div className="d-flex align-items-center">
          <button className="btn btn-outline-secondary me-3" onClick={() => navigate('/opd/queue')}>
            <i className="bi bi-arrow-left me-1"></i> Back to Queue
          </button>
          <div>
            <h2 className="h3 mb-0 text-dark fw-bold">{visit.opdVisitId}</h2>
            <span className="text-muted small">Queue Number: #{String(visit.queueNumber).padStart(2, '0')}</span>
          </div>
        </div>
        <div className="d-flex align-items-center gap-2">
          <StatusBadge status={visit.visitStatus} />
          {(isDoctor || isAdmin) && visit.visitStatus !== 'COMPLETED' && (
            <Link to={`/opd/consultation/${visit.id}`} className="btn btn-primary btn-sm">
              <i className="bi bi-clipboard2-pulse me-1"></i> Start Consultation
            </Link>
          )}
        </div>
      </div>

      <div className="row g-4">
        {/* Patient & Doctor Card */}
        <div className="col-md-12">
          <div className="card border-0 shadow-sm">
            <div className="card-body">
              <div className="row g-3">
                <div className="col-md-4 border-end">
                  <span className="text-muted small">Patient Name</span>
                  <div className="fw-bold fs-5">
                    <Link to={`/patients/${visit.patientId}`} className="text-decoration-none text-dark">
                      {visit.patientName}
                    </Link>
                  </div>
                  <div className="small text-muted">{visit.patientCode} • Age: {visit.patientAge} • {visit.patientGender}</div>
                  <div className="small text-muted">Phone: {visit.patientPhone}</div>
                </div>

                <div className="col-md-4 border-end">
                  <span className="text-muted small">Attending Doctor</span>
                  <div className="fw-bold fs-5">{visit.doctorName}</div>
                  <div className="small text-muted">{visit.doctorSpecialization}</div>
                  <div className="small text-muted">Department: {visit.departmentName}</div>
                </div>

                <div className="col-md-4">
                  <span className="text-muted small">Appointment Link</span>
                  <div className="fw-bold fs-6">
                    <Link to={`/appointments/${visit.appointmentId}`} className="text-primary text-decoration-none">
                      {visit.appointmentCode}
                    </Link>
                  </div>
                  <div className="small text-muted">Visit Date: {visit.visitDate}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Clinical Vitals & Documentation Form */}
        <div className="col-md-12">
          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-primary">
              <i className="bi bi-heart-pulse me-2"></i> Vital Signs & Physical Measurements
            </div>
            <div className="card-body">
              <form onSubmit={handleSaveVitals}>
                <div className="row g-3 mb-4">
                  <div className="col-md-2">
                    <label className="form-label small">Temperature (°C)</label>
                    <input
                      type="number"
                      step="0.1"
                      className="form-control"
                      placeholder="e.g. 37.0"
                      value={vitals.vitalTemperature}
                      onChange={(e) => setVitals({ ...vitals, vitalTemperature: e.target.value })}
                    />
                  </div>
                  <div className="col-md-2">
                    <label className="form-label small">Pulse (bpm)</label>
                    <input
                      type="number"
                      className="form-control"
                      placeholder="e.g. 72"
                      value={vitals.vitalPulse}
                      onChange={(e) => setVitals({ ...vitals, vitalPulse: e.target.value })}
                    />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label small">Blood Pressure (mmHg)</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. 120/80"
                      value={vitals.vitalBloodPressure}
                      onChange={(e) => setVitals({ ...vitals, vitalBloodPressure: e.target.value })}
                    />
                  </div>
                  <div className="col-md-2">
                    <label className="form-label small">Resp Rate (/min)</label>
                    <input
                      type="number"
                      className="form-control"
                      placeholder="e.g. 16"
                      value={vitals.vitalRespiratoryRate}
                      onChange={(e) => setVitals({ ...vitals, vitalRespiratoryRate: e.target.value })}
                    />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label small">SpO2 (%)</label>
                    <input
                      type="number"
                      className="form-control"
                      placeholder="e.g. 98"
                      value={vitals.vitalOxygenSaturation}
                      onChange={(e) => setVitals({ ...vitals, vitalOxygenSaturation: e.target.value })}
                    />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label small">Height (cm)</label>
                    <input
                      type="number"
                      step="0.1"
                      className="form-control"
                      placeholder="e.g. 170"
                      value={vitals.heightCm}
                      onChange={(e) => setVitals({ ...vitals, heightCm: e.target.value })}
                    />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label small">Weight (kg)</label>
                    <input
                      type="number"
                      step="0.1"
                      className="form-control"
                      placeholder="e.g. 68.5"
                      value={vitals.weightKg}
                      onChange={(e) => setVitals({ ...vitals, weightKg: e.target.value })}
                    />
                  </div>
                </div>

                <h5 className="border-bottom pb-2 text-primary mb-3">Clinical Documentation</h5>
                <div className="row g-3 mb-4">
                  <div className="col-md-12">
                    <label className="form-label font-semibold">Chief Complaint</label>
                    <textarea
                      className="form-control"
                      rows="2"
                      value={vitals.chiefComplaint}
                      onChange={(e) => setVitals({ ...vitals, chiefComplaint: e.target.value })}
                    ></textarea>
                  </div>
                  <div className="col-md-12">
                    <label className="form-label font-semibold">Clinical Notes</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      value={vitals.clinicalNotes}
                      onChange={(e) => setVitals({ ...vitals, clinicalNotes: e.target.value })}
                    ></textarea>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label font-semibold">Diagnosis</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      value={vitals.diagnosis}
                      onChange={(e) => setVitals({ ...vitals, diagnosis: e.target.value })}
                    ></textarea>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label font-semibold">Treatment Plan</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      value={vitals.treatmentPlan}
                      onChange={(e) => setVitals({ ...vitals, treatmentPlan: e.target.value })}
                    ></textarea>
                  </div>
                </div>

                <div className="d-flex justify-content-end">
                  <button type="submit" className="btn btn-primary px-4" disabled={saving}>
                    {saving ? 'Saving...' : 'Save Vitals & Notes'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OPDVisitPage;
