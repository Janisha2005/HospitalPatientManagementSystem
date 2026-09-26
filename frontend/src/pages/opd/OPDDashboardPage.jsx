import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { opdService } from '../../services/opdService';
import { doctorService } from '../../services/doctorService';
import LoadingSpinner from '../../components/LoadingSpinner';
import DashboardCard from '../../components/DashboardCard';
import { authService } from '../../services/authService';

const OPDDashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState('');
  const [loading, setLoading] = useState(true);

  const isDoctor = authService.hasRole('DOCTOR');
  const user = authService.getStoredUser();

  useEffect(() => {
    loadMasters();
  }, []);

  useEffect(() => {
    fetchStats();
  }, [selectedDoctorId]);

  const loadMasters = async () => {
    try {
      const res = await doctorService.searchDoctors({ size: 100 });
      if (res.success) setDoctors(res.data.content || []);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchStats = async () => {
    setLoading(true);
    try {
      const res = await opdService.getDashboardStats(selectedDoctorId || null);
      if (res.success) {
        setStats(res.data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="py-5"><LoadingSpinner /></div>;

  return (
    <div className="container-fluid py-3">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 mb-1 text-dark fw-bold">OPD Clinical Dashboard</h2>
          <p className="text-muted mb-0">Real-time Outpatient Department Metrics & Operations</p>
        </div>
        <div className="d-flex gap-2">
          {!isDoctor && (
            <select
              className="form-select"
              value={selectedDoctorId}
              onChange={(e) => setSelectedDoctorId(e.target.value)}
              style={{ width: '250px' }}
            >
              <option value="">All Hospital Doctors</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>Dr. {d.firstName} {d.lastName}</option>
              ))}
            </select>
          )}
          <Link to="/opd/queue" className="btn btn-primary">
            <i className="bi bi-list-task me-1"></i> Live OPD Queue
          </Link>
        </div>
      </div>

      {/* Hospital Overall Metrics */}
      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <DashboardCard
            title="Today's Appointments"
            value={stats?.todaysAppointments || 0}
            icon="bi-calendar-check"
            color="primary"
          />
        </div>
        <div className="col-md-3">
          <DashboardCard
            title="Checked-In Patients"
            value={stats?.checkedInPatients || 0}
            icon="bi-person-check"
            color="info"
          />
        </div>
        <div className="col-md-3">
          <DashboardCard
            title="Waiting in Queue"
            value={stats?.waitingPatients || 0}
            icon="bi-hourglass-split"
            color="warning"
          />
        </div>
        <div className="col-md-3">
          <DashboardCard
            title="In Consultation"
            value={stats?.inConsultationPatients || 0}
            icon="bi-clipboard2-pulse"
            color="success"
          />
        </div>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <DashboardCard
            title="Completed Consultations"
            value={stats?.completedVisits || 0}
            icon="bi-check-all"
            color="success"
          />
        </div>
        <div className="col-md-4">
          <DashboardCard
            title="Cancelled Appointments"
            value={stats?.cancelledAppointments || 0}
            icon="bi-x-circle"
            color="danger"
          />
        </div>
        <div className="col-md-4">
          <DashboardCard
            title="No-Show Appointments"
            value={stats?.noShowAppointments || 0}
            icon="bi-person-x"
            color="secondary"
          />
        </div>
      </div>

      {/* Doctor-Specific Metrics Card if Doctor or Filter selected */}
      {(isDoctor || selectedDoctorId) && (
        <div className="card border-0 shadow-sm mb-4 bg-light">
          <div className="card-body">
            <h5 className="card-title text-primary mb-3">
              <i className="bi bi-person-workspace me-2"></i> Doctor Workload Summary
            </h5>
            <div className="row text-center g-3">
              <div className="col-3">
                <div className="h3 mb-0 text-dark fw-bold">{stats?.myAppointmentsToday || 0}</div>
                <div className="small text-muted">My Today Appointments</div>
              </div>
              <div className="col-3">
                <div className="h3 mb-0 text-warning fw-bold">{stats?.myWaitingPatients || 0}</div>
                <div className="small text-muted">My Waiting Patients</div>
              </div>
              <div className="col-3">
                <div className="h3 mb-0 text-info fw-bold">{stats?.myInConsultationPatients || 0}</div>
                <div className="small text-muted">In Consultation</div>
              </div>
              <div className="col-3">
                <div className="h3 mb-0 text-success fw-bold">{stats?.myCompletedVisits || 0}</div>
                <div className="small text-muted">Completed Consultations</div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OPDDashboardPage;
