import React, { useEffect, useState } from 'react';
import DashboardCard from '../../components/DashboardCard';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import { dashboardService } from '../../services/dashboardService';

const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchStats = async () => {
    setLoading(true);
    try {
      const response = await dashboardService.getStats();
      if (response.success) {
        setStats(response.data);
      } else {
        setError(response.message || 'Failed to load dashboard metrics');
      }
    } catch (err) {
      setError(err.message || 'Error fetching dashboard metrics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold mb-0 text-dark">Dashboard</h3>
          <p className="text-muted small mb-0">Overview of key hospital operations and active entities</p>
        </div>
        <button className="btn btn-sm btn-outline-primary" onClick={fetchStats}>
          <i className="bi bi-arrow-clockwise me-1"></i> Refresh
        </button>
      </div>

      <ErrorMessage message={error} onClose={() => setError('')} />

      {loading ? (
        <LoadingSpinner message="Fetching live database statistics..." />
      ) : (
        <div className="row g-3">
          <div className="col-12 col-sm-6 col-lg-3">
            <DashboardCard
              title="Total Patients"
              value={stats?.totalPatients}
              icon="people"
              color="primary"
            />
          </div>
          <div className="col-12 col-sm-6 col-lg-3">
            <DashboardCard
              title="Total Doctors"
              value={stats?.totalDoctors}
              icon="person-badge"
              color="success"
            />
          </div>
          <div className="col-12 col-sm-6 col-lg-3">
            <DashboardCard
              title="Departments"
              value={stats?.totalDepartments}
              icon="building"
              color="info"
            />
          </div>
          <div className="col-12 col-sm-6 col-lg-3">
            <DashboardCard
              title="Active Users"
              value={stats?.activeUsers}
              icon="shield-check"
              color="warning"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
