import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { appointmentService } from '../../services/appointmentService';
import { opdService } from '../../services/opdService';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import StatusBadge from '../../components/StatusBadge';
import { authService } from '../../services/authService';

const AppointmentDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [appointment, setAppointment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const isAdmin = authService.hasRole('ADMIN');
  const isReceptionist = authService.hasRole('RECEPTIONIST');
  const isNurse = authService.hasRole('NURSE');

  useEffect(() => {
    fetchAppointmentDetails();
  }, [id]);

  const fetchAppointmentDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await appointmentService.getAppointmentById(id);
      if (res.success) {
        setAppointment(res.data);
      } else {
        setError(res.message);
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch appointment details');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async () => {
    setActionLoading(true);
    try {
      const res = await appointmentService.confirmAppointment(id);
      if (res.success) fetchAppointmentDetails();
    } catch (e) {
      alert(e.message || 'Failed to confirm appointment');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCheckIn = async () => {
    setActionLoading(true);
    try {
      const res = await appointmentService.checkInPatient(id);
      if (res.success) {
        navigate('/opd/queue');
      }
    } catch (e) {
      alert(e.message || 'Failed to check in patient');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async () => {
    const reason = prompt('Enter reason for cancellation:');
    if (reason === null) return;
    setActionLoading(true);
    try {
      const res = await appointmentService.cancelAppointment(id, reason);
      if (res.success) fetchAppointmentDetails();
    } catch (e) {
      alert(e.message || 'Failed to cancel appointment');
    } finally {
      setActionLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${d}-${m}-${y}`;
  };

  if (loading) return <div className="py-5"><LoadingSpinner /></div>;
  if (error) return <div className="container py-4"><ErrorMessage message={error} /></div>;
  if (!appointment) return null;

  return (
    <div className="container py-3" style={{ maxWidth: '900px' }}>
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div className="d-flex align-items-center">
          <button className="btn btn-outline-secondary me-3" onClick={() => navigate('/appointments')}>
            <i className="bi bi-arrow-left me-1"></i> Back
          </button>
          <div>
            <h2 className="h3 mb-1 text-dark fw-bold">{appointment.appointmentId}</h2>
            <span className="text-muted small">Appointment Summary</span>
          </div>
        </div>
        <StatusBadge status={appointment.status} />
      </div>

      <div className="row g-4">
        {/* Left Column: Key Details */}
        <div className="col-md-8">
          <div className="card border-0 shadow-sm mb-4">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-primary">
              <i className="bi bi-info-circle me-2"></i> Patient & Doctor Info
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-md-6 border-end">
                  <div className="text-muted small">Patient</div>
                  <div className="fw-bold fs-5 text-dark">
                    <Link to={`/patients/${appointment.patientId}`} className="text-decoration-none text-dark">
                      {appointment.patientName}
                    </Link>
                  </div>
                  <div className="small text-secondary">Code: {appointment.patientCode}</div>
                  <div className="small text-secondary">Phone: {appointment.patientPhone}</div>
                </div>

                <div className="col-md-6">
                  <div className="text-muted small">Assigned Doctor</div>
                  <div className="fw-bold fs-5 text-dark">
                    <Link to={`/doctors/${appointment.doctorId}`} className="text-decoration-none text-dark">
                      {appointment.doctorName}
                    </Link>
                  </div>
                  <div className="small text-secondary">Department: {appointment.departmentName}</div>
                  <div className="small text-secondary">Specialization: {appointment.doctorSpecialization}</div>
                </div>
              </div>
            </div>
          </div>

          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-primary">
              <i className="bi bi-clock me-2"></i> Schedule & Clinical Reason
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-md-6">
                  <div className="text-muted small">Date</div>
                  <div className="fw-semibold text-dark">{formatDate(appointment.appointmentDate)}</div>
                </div>
                <div className="col-md-6">
                  <div className="text-muted small">Time Slot</div>
                  <div className="fw-semibold text-dark">{appointment.startTime} - {appointment.endTime}</div>
                </div>
                <div className="col-md-6">
                  <div className="text-muted small">Appointment Type</div>
                  <div>
                    <span className="badge bg-light text-dark border">
                      {appointment.appointmentType?.replace('_', ' ')}
                    </span>
                  </div>
                </div>
                <div className="col-md-6">
                  <div className="text-muted small">Booked By</div>
                  <div className="text-dark">{appointment.createdByName || 'System'}</div>
                </div>
                <div className="col-12">
                  <div className="text-muted small">Chief Reason for Visit</div>
                  <div className="p-3 bg-light rounded text-dark mt-1">
                    {appointment.reasonForVisit || 'No reason specified'}
                  </div>
                </div>
                {appointment.notes && (
                  <div className="col-12">
                    <div className="text-muted small">Notes</div>
                    <div className="p-3 bg-light rounded text-dark mt-1">
                      {appointment.notes}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Actions */}
        <div className="col-md-4">
          <div className="card border-0 shadow-sm mb-4">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-dark">
              Workflow Actions
            </div>
            <div className="card-body d-flex flex-column gap-2">
              {appointment.status === 'SCHEDULED' && (isAdmin || isReceptionist) && (
                <button
                  className="btn btn-outline-success w-100"
                  onClick={handleConfirm}
                  disabled={actionLoading}
                >
                  <i className="bi bi-check-circle me-1"></i> Confirm Appointment
                </button>
              )}

              {(appointment.status === 'SCHEDULED' || appointment.status === 'CONFIRMED') && (isAdmin || isReceptionist || isNurse) && (
                <button
                  className="btn btn-primary w-100"
                  onClick={handleCheckIn}
                  disabled={actionLoading}
                >
                  <i className="bi bi-person-check me-1"></i> Check-In Patient
                </button>
              )}

              {(appointment.status === 'SCHEDULED' || appointment.status === 'CONFIRMED') && (
                <button
                  className="btn btn-outline-danger w-100"
                  onClick={handleCancel}
                  disabled={actionLoading}
                >
                  <i className="bi bi-x-circle me-1"></i> Cancel Appointment
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AppointmentDetailPage;
