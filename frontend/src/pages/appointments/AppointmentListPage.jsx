import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { appointmentService } from '../../services/appointmentService';
import { doctorService } from '../../services/doctorService';
import { departmentService } from '../../services/departmentService';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import Pagination from '../../components/Pagination';
import StatusBadge from '../../components/StatusBadge';
import { authService } from '../../services/authService';

const AppointmentListPage = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [doctors, setDoctors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [doctorId, setDoctorId] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [status, setStatus] = useState('');
  const [appointmentType, setAppointmentType] = useState('');
  const [date, setDate] = useState('');

  const canCreate = authService.hasRole('ADMIN', 'RECEPTIONIST', 'PATIENT');

  useEffect(() => {
    loadMasters();
  }, []);

  useEffect(() => {
    fetchAppointments();
  }, [page, doctorId, departmentId, status, appointmentType, date]);

  const loadMasters = async () => {
    try {
      const [docRes, deptRes] = await Promise.all([
        doctorService.searchDoctors({ size: 100 }),
        departmentService.getActiveDepartments()
      ]);
      if (docRes.success) setDoctors(docRes.data.content || []);
      if (deptRes.success) setDepartments(deptRes.data || []);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchAppointments = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size: 10,
        ...(doctorId && { doctorId }),
        ...(departmentId && { departmentId }),
        ...(status && { status }),
        ...(appointmentType && { appointmentType }),
        ...(date && { date })
      };
      const res = await appointmentService.getAppointments(params);
      if (res.success) {
        setAppointments(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setTotalElements(res.data.totalElements || 0);
      } else {
        setError(res.message);
      }
    } catch (err) {
      setError(err.message || 'Failed to load appointments');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${d}-${m}-${y}`;
  };

  const handleCheckIn = async (aptId) => {
    try {
      const res = await appointmentService.checkInPatient(aptId);
      if (res.success) {
        fetchAppointments();
        navigate(`/opd/queue`);
      }
    } catch (e) {
      alert(e.message || 'Failed to check in patient');
    }
  };

  return (
    <div className="container-fluid py-3">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 mb-1 text-dark fw-bold">Appointments</h2>
          <p className="text-muted mb-0">Manage hospital clinical appointments and scheduling</p>
        </div>
        <div className="d-flex gap-2">
          <Link to="/appointments/calendar" className="btn btn-outline-primary me-2">
            <i className="bi bi-calendar3 me-1"></i> Calendar View
          </Link>
          {canCreate && (
            <Link to="/appointments/new" className="btn btn-primary">
              <i className="bi bi-plus-lg me-1"></i> Book Appointment
            </Link>
          )}
        </div>
      </div>

      {/* Filter Card */}
      <div className="card border-0 shadow-sm mb-4">
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-3">
              <label className="form-label small text-muted">Date</label>
              <input
                type="date"
                className="form-control"
                value={date}
                onChange={(e) => { setDate(e.target.value); setPage(0); }}
              />
            </div>
            <div className="col-md-3">
              <label className="form-label small text-muted">Department</label>
              <select
                className="form-select"
                value={departmentId}
                onChange={(e) => { setDepartmentId(e.target.value); setPage(0); }}
              >
                <option value="">All Departments</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>{d.departmentName}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label small text-muted">Doctor</label>
              <select
                className="form-select"
                value={doctorId}
                onChange={(e) => { setDoctorId(e.target.value); setPage(0); }}
              >
                <option value="">All Doctors</option>
                {doctors.map((doc) => (
                  <option key={doc.id} value={doc.id}>Dr. {doc.firstName} {doc.lastName}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label small text-muted">Status</label>
              <select
                className="form-select"
                value={status}
                onChange={(e) => { setStatus(e.target.value); setPage(0); }}
              >
                <option value="">All Statuses</option>
                <option value="SCHEDULED">Scheduled</option>
                <option value="CONFIRMED">Confirmed</option>
                <option value="CHECKED_IN">Checked-In</option>
                <option value="IN_CONSULTATION">In Consultation</option>
                <option value="COMPLETED">Completed</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="NO_SHOW">No Show</option>
                <option value="RESCHEDULED">Rescheduled</option>
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label small text-muted">Type</label>
              <select
                className="form-select"
                value={appointmentType}
                onChange={(e) => { setAppointmentType(e.target.value); setPage(0); }}
              >
                <option value="">All Types</option>
                <option value="NEW_CONSULTATION">New Consultation</option>
                <option value="FOLLOW_UP">Follow Up</option>
                <option value="EMERGENCY">Emergency</option>
                <option value="ROUTINE_CHECKUP">Routine Checkup</option>
                <option value="TELECONSULTATION">Teleconsultation</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {error && <ErrorMessage message={error} />}

      {/* Table */}
      <div className="card border-0 shadow-sm">
        <div className="card-body p-0">
          {loading ? (
            <div className="py-5"><LoadingSpinner /></div>
          ) : appointments.length === 0 ? (
            <div className="text-center py-5 text-muted">
              <i className="bi bi-calendar-x fs-1 mb-2 d-block"></i>
              No appointments found matching filters.
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Appointment ID</th>
                    <th>Patient</th>
                    <th>Doctor</th>
                    <th>Department</th>
                    <th>Date & Time</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th className="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((apt) => (
                    <tr key={apt.id}>
                      <td className="fw-semibold">
                        <Link to={`/appointments/${apt.id}`} className="text-decoration-none text-primary">
                          {apt.appointmentId}
                        </Link>
                      </td>
                      <td>
                        <Link to={`/patients/${apt.patientId}`} className="text-dark fw-medium text-decoration-none">
                          {apt.patientName}
                        </Link>
                        <div className="small text-muted">{apt.patientCode}</div>
                      </td>
                      <td>{apt.doctorName}</td>
                      <td>{apt.departmentName}</td>
                      <td>
                        <div>{formatDate(apt.appointmentDate)}</div>
                        <div className="small text-muted">{apt.startTime} - {apt.endTime}</div>
                      </td>
                      <td>
                        <span className="badge bg-light text-dark border">
                          {apt.appointmentType?.replace('_', ' ')}
                        </span>
                      </td>
                      <td>
                        <StatusBadge status={apt.status} />
                      </td>
                      <td className="text-end">
                        <div className="dropdown">
                          <button className="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                            Actions
                          </button>
                          <ul className="dropdown-menu dropdown-menu-end">
                            <li>
                              <Link className="dropdown-item" to={`/appointments/${apt.id}`}>
                                <i className="bi bi-eye me-2"></i> View Details
                              </Link>
                            </li>
                            {(apt.status === 'SCHEDULED' || apt.status === 'CONFIRMED') && (
                              <li>
                                <button className="dropdown-item text-success" onClick={() => handleCheckIn(apt.id)}>
                                  <i className="bi bi-person-check me-2"></i> Check-In Patient
                                </button>
                              </li>
                            )}
                          </ul>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
        {totalPages > 1 && (
          <div className="card-footer bg-white border-0 py-3">
            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
          </div>
        )}
      </div>
    </div>
  );
};

export default AppointmentListPage;
