import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import StatusBadge from '../../components/StatusBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import { patientService } from '../../services/patientService';
import { appointmentService } from '../../services/appointmentService';
import { opdService } from '../../services/opdService';
import { authService } from '../../services/authService';
import { formatIndianDate } from '../../utils/indiaUtils';

const PatientDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isAdmin = authService.hasRole('ADMIN');

  const [patient, setPatient] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [opdVisits, setOpdVisits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchPatientData = async () => {
      try {
        const response = await patientService.getPatientById(id);
        if (response.success && response.data) {
          setPatient(response.data);
          // Fetch appointments and OPD visits for this patient
          const [aptRes, opdRes] = await Promise.all([
            appointmentService.getAppointments({ patientId: id, size: 5 }),
            opdService.getVisits({ patientId: id, size: 5 })
          ]);
          if (aptRes.success) setAppointments(aptRes.data.content || []);
          if (opdRes.success) setOpdVisits(opdRes.data.content || []);
        }
      } catch (err) {
        setError(err.message || 'Failed to load patient');
      } finally {
        setLoading(false);
      }
    };
    fetchPatientData();
  }, [id]);

  if (loading) return <LoadingSpinner message="Loading patient profile..." />;
  if (error) return <ErrorMessage message={error} />;
  if (!patient) return null;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div className="d-flex align-items-center">
          <button className="btn btn-outline-secondary btn-sm me-3" onClick={() => navigate('/patients')}>
            <i className="bi bi-arrow-left me-1"></i> Back to Patients
          </button>
          <div>
            <h3 className="fw-bold mb-0 text-dark">
              {patient.firstName} {patient.lastName}
            </h3>
            <span className="badge bg-primary font-monospace">{patient.patientId}</span>
          </div>
        </div>
        <div className="d-flex gap-2">
          <Link to="/appointments/new" className="btn btn-outline-primary">
            <i className="bi bi-calendar-plus me-1"></i> Book Appointment
          </Link>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => navigate(`/patients/${patient.id}/edit`)}>
              <i className="bi bi-pencil me-1"></i> Edit Profile
            </button>
          )}
        </div>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-md-8">
          <div className="card border-0 shadow-sm rounded-3 mb-3">
            <div className="card-header bg-white border-bottom py-3">
              <h6 className="card-title mb-0 fw-bold text-primary">
                <i className="bi bi-person-lines-fill me-2"></i>Demographics & Personal Details
              </h6>
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-sm-6">
                  <div className="small text-muted">Full Name</div>
                  <div className="fw-semibold">{patient.firstName} {patient.lastName}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Patient ID</div>
                  <div className="fw-bold text-primary font-monospace">{patient.patientId}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Date of Birth (DD-MM-YYYY)</div>
                  <div className="fw-semibold">{formatIndianDate(patient.dateOfBirth)}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Gender</div>
                  <div className="fw-semibold">{patient.gender}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Blood Group</div>
                  <div>
                    <span className="badge bg-danger bg-opacity-10 text-danger border border-danger">
                      {patient.bloodGroup || 'N/A'}
                    </span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Account Status</div>
                  <div><StatusBadge isActive={patient.isActive} /></div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Mobile Number</div>
                  <div className="fw-semibold font-monospace">{patient.phone}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Email</div>
                  <div className="fw-semibold">{patient.email || 'N/A'}</div>
                </div>
                <div className="col-12 border-top pt-3">
                  <div className="small text-muted fw-bold mb-1">
                    <i className="bi bi-geo-alt me-1 text-primary"></i>Residential Address (India)
                  </div>
                  <div className="fw-normal text-secondary">
                    {patient.addressLine1 && <div>{patient.addressLine1}</div>}
                    {patient.addressLine2 && <div>{patient.addressLine2}</div>}
                    <div>
                      {[patient.city, patient.district, patient.state].filter(Boolean).join(', ')}
                      {patient.pincode ? ` - ${patient.pincode}` : ''}
                    </div>
                    <div>{patient.country || 'India'}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-header bg-white border-bottom py-3">
              <h6 className="card-title mb-0 fw-bold text-danger">
                <i className="bi bi-telephone-outbound me-2"></i>Emergency Contact (India)
              </h6>
            </div>
            <div className="card-body">
              <div className="mb-3">
                <div className="small text-muted">Contact Person</div>
                <div className="fw-bold">{patient.emergencyContactName}</div>
              </div>
              <div className="mb-3">
                <div className="small text-muted">Phone Number</div>
                <div className="fw-semibold text-danger font-monospace">{patient.emergencyContactPhone}</div>
              </div>
              <div>
                <div className="small text-muted">Relationship</div>
                <div className="badge bg-light text-dark border">{patient.emergencyContactRelationship}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Integrated Phase 2: Appointments & OPD History */}
      <div className="row g-3">
        <div className="col-md-6">
          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-dark d-flex justify-content-between">
              <span><i className="bi bi-calendar-event me-2 text-primary"></i> Recent Appointments</span>
              <Link to="/appointments" className="small text-decoration-none">View All</Link>
            </div>
            <div className="card-body p-0">
              {appointments.length === 0 ? (
                <div className="text-center py-4 text-muted">No appointments found.</div>
              ) : (
                <ul className="list-group list-group-flush">
                  {appointments.map((apt) => (
                    <li key={apt.id} className="list-group-item d-flex justify-content-between align-items-center py-3">
                      <div>
                        <div className="fw-bold">{apt.appointmentId} • {apt.doctorName}</div>
                        <div className="small text-muted">{formatIndianDate(apt.appointmentDate)} at {apt.startTime}</div>
                      </div>
                      <StatusBadge status={apt.status} />
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-dark d-flex justify-content-between">
              <span><i className="bi bi-journal-check me-2 text-primary"></i> OPD Visit History</span>
              <Link to="/opd/queue" className="small text-decoration-none">Live Queue</Link>
            </div>
            <div className="card-body p-0">
              {opdVisits.length === 0 ? (
                <div className="text-center py-4 text-muted">No OPD visit records found.</div>
              ) : (
                <ul className="list-group list-group-flush">
                  {opdVisits.map((v) => (
                    <li key={v.id} className="list-group-item d-flex justify-content-between align-items-center py-3">
                      <div>
                        <div className="fw-bold">{v.opdVisitId} • Dr. {v.doctorName}</div>
                        <div className="small text-muted">{formatIndianDate(v.visitDate)} • Diagnosis: {v.diagnosis || 'Pending'}</div>
                      </div>
                      <StatusBadge status={v.visitStatus} />
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PatientDetailPage;
