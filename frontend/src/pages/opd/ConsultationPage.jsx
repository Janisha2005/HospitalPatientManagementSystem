import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { opdService } from '../../services/opdService';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import StatusBadge from '../../components/StatusBadge';

const ConsultationPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [visit, setVisit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [consultData, setConsultData] = useState({
    chiefComplaint: '',
    clinicalNotes: '',
    diagnosis: '',
    treatmentPlan: '',
    vitalTemperature: '',
    vitalPulse: '',
    vitalBloodPressure: '',
    vitalRespiratoryRate: '',
    vitalOxygenSaturation: '',
    heightCm: '',
    weightKg: ''
  });

  useEffect(() => {
    fetchConsultation();
  }, [id]);

  const fetchConsultation = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await opdService.getVisitById(id);
      if (res.success) {
        setVisit(res.data);
        setConsultData({
          chiefComplaint: res.data.chiefComplaint || '',
          clinicalNotes: res.data.clinicalNotes || '',
          diagnosis: res.data.diagnosis || '',
          treatmentPlan: res.data.treatmentPlan || '',
          vitalTemperature: res.data.vitalTemperature || '',
          vitalPulse: res.data.vitalPulse || '',
          vitalBloodPressure: res.data.vitalBloodPressure || '',
          vitalRespiratoryRate: res.data.vitalRespiratoryRate || '',
          vitalOxygenSaturation: res.data.vitalOxygenSaturation || '',
          heightCm: res.data.heightCm || '',
          weightKg: res.data.weightKg || ''
        });

        // Auto-start consultation status if currently WAITING/CALLED
        if (res.data.visitStatus === 'WAITING' || res.data.visitStatus === 'CALLED') {
          await opdService.startConsultation(id);
        }
      } else {
        setError(res.message);
      }
    } catch (err) {
      setError(err.message || 'Failed to initialize consultation');
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async (e) => {
    e.preventDefault();
    if (!consultData.chiefComplaint && !consultData.diagnosis) {
      alert('Please enter at least chief complaint or diagnosis before completing consultation.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await opdService.completeConsultation(id, consultData);
      if (res.success) {
        alert('Consultation completed successfully');
        navigate('/opd/queue');
      } else {
        alert(res.message);
      }
    } catch (err) {
      alert(err.message || 'Failed to complete consultation');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="py-5"><LoadingSpinner /></div>;
  if (error) return <div className="container py-4"><ErrorMessage message={error} /></div>;
  if (!visit) return null;

  return (
    <div className="container-fluid py-3">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 mb-1 text-dark fw-bold">Doctor OPD Consultation Workbench</h2>
          <span className="text-muted">Queue #{String(visit.queueNumber).padStart(2, '0')} • OPD ID: {visit.opdVisitId}</span>
        </div>
        <div className="d-flex align-items-center gap-2">
          <StatusBadge status={visit.visitStatus} />
          <button className="btn btn-outline-secondary" onClick={() => navigate('/opd/queue')}>
            <i className="bi bi-x-lg me-1"></i> Exit Workbench
          </button>
        </div>
      </div>

      <div className="row g-4">
        {/* Left Column: Patient Summary Card */}
        <div className="col-md-4">
          <div className="card border-0 shadow-sm mb-4">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-primary">
              <i className="bi bi-person-bounding-box me-2"></i> Patient Information
            </div>
            <div className="card-body">
              <h4 className="h5 fw-bold mb-1">{visit.patientName}</h4>
              <div className="small text-muted mb-3">{visit.patientCode} • {visit.patientGender} • Age {visit.patientAge}</div>

              <div className="border-top pt-3 mb-3">
                <div className="row g-2 text-center">
                  <div className="col-6">
                    <div className="p-2 bg-light rounded">
                      <div className="small text-muted">Phone</div>
                      <div className="fw-semibold text-dark">{visit.patientPhone}</div>
                    </div>
                  </div>
                  <div className="col-6">
                    <div className="p-2 bg-light rounded">
                      <div className="small text-muted">Appointment</div>
                      <div className="fw-semibold text-primary">{visit.appointmentCode}</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Vitals Summary Card */}
              <div className="border-top pt-3">
                <h6 className="fw-bold mb-2 text-secondary">Recorded Vital Signs</h6>
                <div className="row g-2">
                  <div className="col-6">
                    <div className="small text-muted">BP</div>
                    <div className="fw-bold">{consultData.vitalBloodPressure || 'N/A'}</div>
                  </div>
                  <div className="col-6">
                    <div className="small text-muted">Pulse</div>
                    <div className="fw-bold">{consultData.vitalPulse ? `${consultData.vitalPulse} bpm` : 'N/A'}</div>
                  </div>
                  <div className="col-6">
                    <div className="small text-muted">Temp</div>
                    <div className="fw-bold">{consultData.vitalTemperature ? `${consultData.vitalTemperature} °C` : 'N/A'}</div>
                  </div>
                  <div className="col-6">
                    <div className="small text-muted">SpO2</div>
                    <div className="fw-bold">{consultData.vitalOxygenSaturation ? `${consultData.vitalOxygenSaturation} %` : 'N/A'}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Clinical Note Editor */}
        <div className="col-md-8">
          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-primary d-flex justify-content-between align-items-center">
              <span><i className="bi bi-journal-medical me-2"></i> Clinical Notes & Consultation Entry</span>
              <span className="small text-muted">India HMS Localized Clinical Format</span>
            </div>
            <div className="card-body">
              <form onSubmit={handleComplete}>
                <div className="mb-3">
                  <label className="form-label fw-semibold required">Chief Complaint *</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    placeholder="Patient presented with complaints of..."
                    value={consultData.chiefComplaint}
                    onChange={(e) => setConsultData({ ...consultData, chiefComplaint: e.target.value })}
                    required
                  ></textarea>
                </div>

                <div className="mb-3">
                  <label className="form-label fw-semibold">Clinical Examination & Observations</label>
                  <textarea
                    className="form-control"
                    rows="3"
                    placeholder="Detailed symptoms, history, examination findings..."
                    value={consultData.clinicalNotes}
                    onChange={(e) => setConsultData({ ...consultData, clinicalNotes: e.target.value })}
                  ></textarea>
                </div>

                <div className="mb-3">
                  <label className="form-label fw-semibold required">Diagnosis *</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    placeholder="Primary and secondary diagnosis..."
                    value={consultData.diagnosis}
                    onChange={(e) => setConsultData({ ...consultData, diagnosis: e.target.value })}
                    required
                  ></textarea>
                </div>

                <div className="mb-4">
                  <label className="form-label fw-semibold">Treatment Plan & Recommendations</label>
                  <textarea
                    className="form-control"
                    rows="3"
                    placeholder="Advice, prescribed routine, follow-up recommendations..."
                    value={consultData.treatmentPlan}
                    onChange={(e) => setConsultData({ ...consultData, treatmentPlan: e.target.value })}
                  ></textarea>
                </div>

                <div className="d-flex justify-content-end gap-2">
                  <button type="button" className="btn btn-outline-secondary" onClick={() => navigate('/opd/queue')}>
                    Save Draft / Exit
                  </button>
                  <button type="submit" className="btn btn-success px-4" disabled={submitting}>
                    <i className="bi bi-check2-circle me-1"></i>
                    {submitting ? 'Completing...' : 'Complete Consultation'}
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

export default ConsultationPage;
