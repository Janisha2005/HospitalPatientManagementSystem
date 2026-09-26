import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { appointmentService } from '../../services/appointmentService';
import { doctorService } from '../../services/doctorService';
import { departmentService } from '../../services/departmentService';
import LoadingSpinner from '../../components/LoadingSpinner';
import StatusBadge from '../../components/StatusBadge';

const AppointmentCalendarPage = () => {
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [doctorId, setDoctorId] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadMasters();
  }, []);

  useEffect(() => {
    fetchDayAppointments();
  }, [selectedDate, doctorId, departmentId]);

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

  const fetchDayAppointments = async () => {
    setLoading(true);
    try {
      const params = {
        date: selectedDate,
        size: 100,
        ...(doctorId && { doctorId }),
        ...(departmentId && { departmentId })
      };
      const res = await appointmentService.getAppointments(params);
      if (res.success) {
        setAppointments(res.data.content || []);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handlePrevDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() - 1);
    setSelectedDate(d.toISOString().split('T')[0]);
  };

  const handleNextDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + 1);
    setSelectedDate(d.toISOString().split('T')[0]);
  };

  const handleToday = () => {
    setSelectedDate(new Date().toISOString().split('T')[0]);
  };

  const formatDateDisplay = (dateStr) => {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${d}-${m}-${y}`;
  };

  return (
    <div className="container-fluid py-3">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 mb-1 text-dark fw-bold">Appointment Day Schedule</h2>
          <p className="text-muted mb-0">Daily appointment view for hospital doctors and departments</p>
        </div>
        <Link to="/appointments" className="btn btn-outline-secondary">
          <i className="bi bi-list-ul me-1"></i> List View
        </Link>
      </div>

      {/* Control Bar */}
      <div className="card border-0 shadow-sm mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-center">
            <div className="col-md-4 d-flex align-items-center gap-2">
              <button className="btn btn-outline-secondary" onClick={handlePrevDay}>
                <i className="bi bi-chevron-left"></i>
              </button>
              <button className="btn btn-outline-primary" onClick={handleToday}>
                Today
              </button>
              <button className="btn btn-outline-secondary" onClick={handleNextDay}>
                <i className="bi bi-chevron-right"></i>
              </button>
              <input
                type="date"
                className="form-control"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
              />
            </div>
            <div className="col-md-4">
              <select
                className="form-select"
                value={departmentId}
                onChange={(e) => setDepartmentId(e.target.value)}
              >
                <option value="">All Departments</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>{d.departmentName}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <select
                className="form-select"
                value={doctorId}
                onChange={(e) => setDoctorId(e.target.value)}
              >
                <option value="">All Doctors</option>
                {doctors.map((doc) => (
                  <option key={doc.id} value={doc.id}>Dr. {doc.firstName} {doc.lastName}</option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Daily Schedule Display */}
      <div className="card border-0 shadow-sm">
        <div className="card-header bg-white py-3 border-0 d-flex justify-content-between align-items-center">
          <span className="fw-bold text-dark fs-5">
            Schedule for {formatDateDisplay(selectedDate)}
          </span>
          <span className="badge bg-primary fs-6">{appointments.length} Appointments</span>
        </div>
        <div className="card-body">
          {loading ? (
            <div className="py-5"><LoadingSpinner /></div>
          ) : appointments.length === 0 ? (
            <div className="text-center py-5 text-muted">
              <i className="bi bi-calendar-event fs-1 mb-2 d-block"></i>
              No appointments scheduled for this date.
            </div>
          ) : (
            <div className="row g-3">
              {appointments.map((apt) => (
                <div key={apt.id} className="col-md-6 col-lg-4">
                  <div className="card border shadow-sm h-100">
                    <div className="card-body">
                      <div className="d-flex justify-content-between align-items-start mb-2">
                        <span className="badge bg-light text-primary border fw-bold">{apt.startTime} - {apt.endTime}</span>
                        <StatusBadge status={apt.status} />
                      </div>
                      <h5 className="card-title h6 mb-1">
                        <Link to={`/appointments/${apt.id}`} className="text-decoration-none text-dark fw-bold">
                          {apt.patientName}
                        </Link>
                      </h5>
                      <div className="small text-muted mb-2">Code: {apt.patientCode}</div>
                      <div className="small text-dark mb-1">
                        <i className="bi bi-person-badge me-1"></i> {apt.doctorName}
                      </div>
                      <div className="small text-secondary mb-2">
                        <i className="bi bi-building me-1"></i> {apt.departmentName}
                      </div>
                      {apt.reasonForVisit && (
                        <div className="small bg-light p-2 rounded text-truncate">
                          Reason: {apt.reasonForVisit}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AppointmentCalendarPage;
