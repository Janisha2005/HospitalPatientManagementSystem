import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { opdService } from '../../services/opdService';
import { doctorService } from '../../services/doctorService';
import { departmentService } from '../../services/departmentService';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import StatusBadge from '../../components/StatusBadge';
import { authService } from '../../services/authService';

const OPDQueuePage = () => {
  const navigate = useNavigate();

  const [queue, setQueue] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [doctorId, setDoctorId] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const isDoctor = authService.hasRole('DOCTOR');
  const isAdmin = authService.hasRole('ADMIN');
  const isNurse = authService.hasRole('NURSE');

  useEffect(() => {
    loadMasters();
  }, []);

  useEffect(() => {
    fetchQueue();
  }, [doctorId, departmentId]);

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

  const fetchQueue = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        ...(doctorId && { doctorId }),
        ...(departmentId && { departmentId })
      };
      const res = await opdService.getTodayQueue(params);
      if (res.success) {
        setQueue(res.data || []);
      } else {
        setError(res.message);
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch OPD Queue');
    } finally {
      setLoading(false);
    }
  };

  const handleCallPatient = async (visitId) => {
    try {
      const res = await opdService.callQueuePatient(visitId);
      if (res.success) fetchQueue();
    } catch (e) {
      alert(e.message || 'Failed to call patient');
    }
  };

  const handleStartConsultation = async (visitId) => {
    try {
      const res = await opdService.startConsultation(visitId);
      if (res.success) {
        navigate(`/opd/consultation/${visitId}`);
      }
    } catch (e) {
      alert(e.message || 'Failed to start consultation');
    }
  };

  return (
    <div className="container-fluid py-3">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 mb-1 text-dark fw-bold">OPD Patient Queue</h2>
          <p className="text-muted mb-0">Live patient queue management for consultation call & vital signs recording</p>
        </div>
        <div className="d-flex gap-2">
          <button className="btn btn-outline-secondary me-2" onClick={fetchQueue}>
            <i className="bi bi-arrow-clockwise me-1"></i> Refresh Queue
          </button>
          <Link to="/opd/dashboard" className="btn btn-outline-primary">
            <i className="bi bi-speedometer2 me-1"></i> OPD Dashboard
          </Link>
        </div>
      </div>

      {/* Filter Card */}
      <div className="card border-0 shadow-sm mb-4">
        <div className="card-body py-3">
          <div className="row g-3">
            <div className="col-md-4">
              <label className="form-label small text-muted">Department Filter</label>
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
              <label className="form-label small text-muted">Doctor Filter</label>
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

      {error && <ErrorMessage message={error} />}

      {/* Queue List Table */}
      <div className="card border-0 shadow-sm">
        <div className="card-body p-0">
          {loading ? (
            <div className="py-5"><LoadingSpinner /></div>
          ) : queue.length === 0 ? (
            <div className="text-center py-5 text-muted">
              <i className="bi bi-people fs-1 mb-2 d-block"></i>
              No active OPD queue records for today.
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Queue No</th>
                    <th>OPD ID</th>
                    <th>Patient</th>
                    <th>Doctor</th>
                    <th>Vitals Recorded</th>
                    <th>Status</th>
                    <th className="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {queue.map((item) => (
                    <tr key={item.id}>
                      <td>
                        <span className="badge bg-primary fs-6 px-3 py-2">
                          #{String(item.queueNumber).padStart(2, '0')}
                        </span>
                      </td>
                      <td className="fw-semibold">
                        <Link to={`/opd/visits/${item.id}`} className="text-decoration-none text-primary">
                          {item.opdVisitId}
                        </Link>
                      </td>
                      <td>
                        <div className="fw-bold text-dark">{item.patientName}</div>
                        <div className="small text-muted">{item.patientCode} • Age: {item.patientAge} • {item.patientGender}</div>
                      </td>
                      <td>
                        <div>{item.doctorName}</div>
                        <div className="small text-muted">{item.departmentName}</div>
                      </td>
                      <td>
                        {item.vitalBloodPressure || item.vitalTemperature ? (
                          <span className="badge bg-success-subtle text-success border border-success">
                            BP: {item.vitalBloodPressure || 'N/A'} | Temp: {item.vitalTemperature ? `${item.vitalTemperature}°C` : 'N/A'}
                          </span>
                        ) : (
                          <span className="badge bg-light text-muted border">Pending Vitals</span>
                        )}
                      </td>
                      <td>
                        <StatusBadge status={item.visitStatus} />
                      </td>
                      <td className="text-end">
                        <div className="d-flex justify-content-end gap-1">
                          <Link to={`/opd/visits/${item.id}`} className="btn btn-sm btn-outline-secondary" title="View/Edit Details">
                            <i className="bi bi-eye"></i> Details
                          </Link>

                          {item.visitStatus === 'WAITING' && (isAdmin || isDoctor || isNurse) && (
                            <button
                              className="btn btn-sm btn-outline-warning"
                              onClick={() => handleCallPatient(item.id)}
                            >
                              <i className="bi bi-megaphone me-1"></i> Call Patient
                            </button>
                          )}

                          {(item.visitStatus === 'WAITING' || item.visitStatus === 'CALLED' || item.visitStatus === 'IN_CONSULTATION') && (isDoctor || isAdmin) && (
                            <button
                              className="btn btn-sm btn-primary"
                              onClick={() => handleStartConsultation(item.id)}
                            >
                              <i className="bi bi-journal-text me-1"></i> Consultation
                            </button>
                          )}
                        </div>
                      </td>
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

export default OPDQueuePage;
