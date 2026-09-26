import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import DataTable from '../../components/DataTable';
import StatusBadge from '../../components/StatusBadge';
import Pagination from '../../components/Pagination';
import SearchBar from '../../components/SearchBar';
import ErrorMessage from '../../components/ErrorMessage';
import { patientService } from '../../services/patientService';
import { authService } from '../../services/authService';

const PatientListPage = () => {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const navigate = useNavigate();
  const isAdmin = authService.hasRole('ADMIN');

  const fetchPatients = async () => {
    setLoading(true);
    try {
      const response = await patientService.searchPatients({
        query: searchQuery,
        page,
        size: 10
      });
      if (response.success) {
        setPatients(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (err) {
      setError(err.message || 'Failed to load patients');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPatients();
  }, [page]);

  const handleSearchSubmit = () => {
    setPage(0);
    fetchPatients();
  };

  const handleStatusToggle = async (id, currentStatus) => {
    try {
      const response = await patientService.updatePatientStatus(id, !currentStatus);
      if (response.success) {
        fetchPatients();
      }
    } catch (err) {
      setError(err.message || 'Failed to update patient status');
    }
  };

  const columns = [
    {
      header: 'Patient ID',
      cell: (row) => (
        <span className="badge bg-light text-primary border font-monospace fw-bold">
          {row.patientId}
        </span>
      )
    },
    {
      header: 'Patient Name',
      cell: (row) => (
        <Link to={`/patients/${row.id}`} className="text-decoration-none fw-bold text-dark">
          {row.firstName} {row.lastName}
        </Link>
      )
    },
    { header: 'Gender', accessor: 'gender' },
    { header: 'Phone', accessor: 'phone' },
    { header: 'Email', accessor: 'email' },
    {
      header: 'Blood Group',
      cell: (row) => (
        <span className="badge bg-danger bg-opacity-10 text-danger border border-danger">
          {row.bloodGroup || 'N/A'}
        </span>
      )
    },
    {
      header: 'Status',
      cell: (row) => <StatusBadge isActive={row.isActive} />
    },
    {
      header: 'Actions',
      cell: (row) => (
        <div className="btn-group btn-group-sm">
          <button
            className="btn btn-outline-info"
            onClick={() => navigate(`/patients/${row.id}`)}
            title="View Details"
          >
            <i className="bi bi-eye"></i>
          </button>
          {isAdmin && (
            <button
              className="btn btn-outline-primary"
              onClick={() => navigate(`/patients/${row.id}/edit`)}
              title="Edit Patient"
            >
              <i className="bi bi-pencil"></i>
            </button>
          )}
          {isAdmin && (
            <button
              className={`btn ${row.isActive ? 'btn-outline-danger' : 'btn-outline-success'}`}
              onClick={() => handleStatusToggle(row.id, row.isActive)}
              title={row.isActive ? 'Deactivate' : 'Activate'}
            >
              <i className={`bi ${row.isActive ? 'bi-person-x' : 'bi-person-check'}`}></i>
            </button>
          )}
        </div>
      )
    }
  ];

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold mb-0 text-dark">Patient Management</h3>
          <p className="text-muted small mb-0">Register, search, and manage patient profiles</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => navigate('/patients/new')}>
            <i className="bi bi-person-plus me-1"></i> Register Patient
          </button>
        )}
      </div>

      <ErrorMessage message={error} onClose={() => setError('')} />

      <div className="card border-0 shadow-sm rounded-3 mb-4">
        <div className="card-body p-3">
          <div className="row g-2 align-items-center">
            <div className="col-12 col-md-6">
              <SearchBar
                value={searchQuery}
                onChange={setSearchQuery}
                onSearch={handleSearchSubmit}
                placeholder="Search by Patient ID, Name, Phone, or Email..."
              />
            </div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-0">
          <DataTable columns={columns} data={patients} loading={loading} emptyMessage="No patients found matching criteria" />
        </div>
        <div className="card-footer bg-white border-0 px-3 py-2">
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>
    </div>
  );
};

export default PatientListPage;
