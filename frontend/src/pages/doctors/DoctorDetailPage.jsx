import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import StatusBadge from '../../components/StatusBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import { doctorService } from '../../services/doctorService';
import { doctorAvailabilityService } from '../../services/doctorAvailabilityService';
import { appointmentService } from '../../services/appointmentService';
import { authService } from '../../services/authService';
import { formatINR, formatIndianDate } from '../../utils/indiaUtils';

const DoctorDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isAdmin = authService.hasRole('ADMIN');

  const [doctor, setDoctor] = useState(null);
  const [availabilities, setAvailabilities] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDoctorData = async () => {
      try {
        const response = await doctorService.getDoctorById(id);
        if (response.success && response.data) {
          setDoctor(response.data);
          const [availRes, aptRes] = await Promise.all([
            doctorAvailabilityService.getDoctorAvailability(id),
            appointmentService.getAppointments({ doctorId: id, size: 5 })
          ]);
          if (availRes.success) setAvailabilities(availRes.data || []);
          if (aptRes.success) setAppointments(aptRes.data.content || []);
        }
      } catch (err) {
        setError(err.message || 'Failed to load doctor details');
      } finally {
        setLoading(false);
      }
    };
    fetchDoctorData();
  }, [id]);

  if (loading) return <LoadingSpinner message="Loading doctor profile..." />;
  if (error) return <ErrorMessage message={error} />;
  if (!doctor) return null;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div className="d-flex align-items-center">
          <button className="btn btn-outline-secondary btn-sm me-3" onClick={() => navigate('/doctors')}>
            <i className="bi bi-arrow-left me-1"></i> Back to Doctors
          </button>
          <div>
            <h3 className="fw-bold mb-0 text-dark">
              Dr. {doctor.firstName} {doctor.lastName}
            </h3>
            <span className="badge bg-success font-monospace">{doctor.doctorId}</span>
          </div>
        </div>
        <div className="d-flex gap-2">
          <Link to={`/doctors/${doctor.id}/availability`} className="btn btn-outline-primary">
            <i className="bi bi-clock-history me-1"></i> Manage Availability Schedule
          </Link>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => navigate(`/doctors/${doctor.id}/edit`)}>
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
                <i className="bi bi-file-medical me-2"></i>Medical Credentials & Assignment
              </h6>
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-sm-6">
                  <div className="small text-muted">Specialization</div>
                  <div className="fw-bold text-dark">{doctor.specialization}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Qualification</div>
                  <div className="fw-semibold">{doctor.qualification}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Medical Registration / License</div>
                  <div className="fw-bold font-monospace text-secondary">{doctor.licenseNumber}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Department</div>
                  <div>
                    <span className="badge bg-info bg-opacity-10 text-info border border-info">
                      {doctor.departmentName} ({doctor.departmentCode})
                    </span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Consultation Fee (INR)</div>
                  <div className="fw-bold fs-5 text-success">{formatINR(doctor.consultationFee)}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Joining Date (DD-MM-YYYY)</div>
                  <div className="fw-semibold">{formatIndianDate(doctor.joiningDate)}</div>
                </div>
                <div className="col-sm-6">
                  <div className="small text-muted">Account Status</div>
                  <div><StatusBadge isActive={doctor.isActive} /></div>
                </div>
                <div className="col-12 border-top pt-3">
                  <div className="small text-muted fw-bold mb-1">
                    <i className="bi bi-geo-alt me-1 text-primary"></i>Residential / Clinic Address (India)
                  </div>
                  <div className="fw-normal text-secondary">
                    {doctor.addressLine1 && <div>{doctor.addressLine1}</div>}
                    {doctor.addressLine2 && <div>{doctor.addressLine2}</div>}
                    <div>
                      {[doctor.city, doctor.district, doctor.state].filter(Boolean).join(', ')}
                      {doctor.pincode ? ` - ${doctor.pincode}` : ''}
                    </div>
                    <div>{doctor.country || 'India'}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card border-0 shadow-sm rounded-3 mb-3">
            <div className="card-header bg-white border-bottom py-3">
              <h6 className="card-title mb-0 fw-bold text-primary">
                <i className="bi bi-envelope me-2"></i>Contact Details (India)
              </h6>
            </div>
            <div className="card-body">
              <div className="mb-3">
                <div className="small text-muted">Email Address</div>
                <div className="fw-semibold">{doctor.email}</div>
              </div>
              <div className="mb-3">
                <div className="small text-muted">Mobile Number</div>
                <div className="fw-semibold font-monospace">{doctor.phone}</div>
              </div>
            </div>
          </div>

          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
              <h6 className="card-title mb-0 fw-bold text-dark">
                <i className="bi bi-clock me-2 text-primary"></i>Weekly Availability
              </h6>
              <Link to={`/doctors/${doctor.id}/availability`} className="small text-decoration-none">Edit</Link>
            </div>
            <div className="card-body p-0">
              {availabilities.length === 0 ? (
                <div className="p-3 text-center text-muted small">No schedule configured.</div>
              ) : (
                <ul className="list-group list-group-flush small">
                  {availabilities.map((a) => (
                    <li key={a.id} className="list-group-item d-flex justify-content-between py-2">
                      <span className="fw-semibold">{a.dayOfWeek}</span>
                      <span>{a.startTime} - {a.endTime} ({a.slotDurationMinutes}m)</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Doctor Appointments Card */}
      <div className="card border-0 shadow-sm">
        <div className="card-header bg-white py-3 border-0 fw-semibold text-dark d-flex justify-content-between">
          <span><i className="bi bi-calendar-check me-2 text-primary"></i> Doctor Appointments</span>
          <Link to={`/appointments?doctorId=${doctor.id}`} className="small text-decoration-none">View All</Link>
        </div>
        <div className="card-body p-0">
          {appointments.length === 0 ? (
            <div className="text-center py-4 text-muted">No appointments assigned to this doctor.</div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Appointment ID</th>
                    <th>Patient</th>
                    <th>Date & Time</th>
                    <th>Type</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((apt) => (
                    <tr key={apt.id}>
                      <td className="fw-bold">
                        <Link to={`/appointments/${apt.id}`} className="text-decoration-none">
                          {apt.appointmentId}
                        </Link>
                      </td>
                      <td>{apt.patientName}</td>
                      <td>{formatIndianDate(apt.appointmentDate)} {apt.startTime}</td>
                      <td>{apt.appointmentType?.replace('_', ' ')}</td>
                      <td><StatusBadge status={apt.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DoctorDetailPage;
