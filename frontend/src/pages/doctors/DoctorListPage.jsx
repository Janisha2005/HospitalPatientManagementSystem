import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import DataTable from '../../components/DataTable';
import StatusBadge from '../../components/StatusBadge';
import Pagination from '../../components/Pagination';
import SearchBar from '../../components/SearchBar';
import Filter from '../../components/Filter';
import ErrorMessage from '../../components/ErrorMessage';
import { doctorService } from '../../services/doctorService';
import { departmentService } from '../../services/departmentService';
import { authService } from '../../services/authService';
import { formatINR } from '../../utils/indiaUtils';

const DoctorListPage = () => {
  const [doctors, setDoctors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const navigate = useNavigate();
  const isAdmin = authService.hasRole('ADMIN');

  useEffect(() => {
    const fetchDepartments = async () => {
      try {
        const response = await departmentService.getAllActiveDepartments();
        if (response.success) {
          setDepartments(response.data);
        }
      } catch (err) {
        // Ignore department filter error
      }
    };
    fetchDepartments();
  }, []);

  const fetchDoctors = async () => {
    setLoading(true);
    try {
      const response = await doctorService.searchDoctors({
        query: searchQuery,
        departmentId: departmentFilter,
        page,
        size: 10
      });
      if (response.success) {
        setDoctors(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (err) {
      setError(err.message || 'Failed to load doctors');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDoctors();
  }, [departmentFilter, page]);

  const handleSearchSubmit = () => {
    setPage(0);
    fetchDoctors();
  };

  const handleStatusToggle = async (id, currentStatus) => {
    try {
      const response = await doctorService.updateDoctorStatus(id, !currentStatus);
      if (response.success) {
        fetchDoctors();
      }
    } catch (err) {
      setError(err.message || 'Failed to update doctor status');
    }
  };

  const departmentOptions = [
    { label: 'All Departments', value: '' },
    ...departments.map((d) => ({ label: d.departmentName, value: d.id }))
  ];

  const columns = [
    {
      header: 'Doctor ID',
      cell: (row) => (
        <span className="badge bg-light text-success border font-monospace fw-bold">
          {row.doctorId}
        </span>
      )
    },
    {
      header: 'Doctor Name',
      cell: (row) => (
        <Link to={`/doctors/${row.id}`} className="text-decoration-none fw-bold text-dark">
          Dr. {row.firstName} {row.lastName}
        </Link>
      )
    },
    { header: 'Specialization', accessor: 'specialization' },
    {
      header: 'Department',
      cell: (row) => (
        <span className="badge bg-info bg-opacity-10 text-info border border-info">
          {row.departmentName}
        </span>
      )
    },
    { header: 'Medical Registration / License', accessor: 'licenseNumber' },
    { header: 'Phone', accessor: 'phone' },
    {
      header: 'Consultation Fee',
      cell: (row) => <span className="fw-bold text-success">{formatINR(row.consultationFee)}</span>
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
            onClick={() => navigate(`/doctors/${row.id}`)}
            title="View Profile"
          >
            <i className="bi bi-eye"></i>
          </button>
          {isAdmin && (
            <button
              className="btn btn-outline-primary"
              onClick={() => navigate(`/doctors/${row.id}/edit`)}
              title="Edit Doctor"
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
          <h3 className="fw-bold mb-0 text-dark">Doctor Directory</h3>
          <p className="text-muted small mb-0">Medical practitioners, qualifications, registration, and consultation fees</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => navigate('/doctors/new')}>
            <i className="bi bi-person-plus me-1"></i> Register Doctor
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
                placeholder="Search by Doctor ID, Name, Specialization, Registration..."
              />
            </div>
            <div className="col-12 col-md-4">
              <Filter
                label="Department"
                options={departmentOptions}
                value={departmentFilter}
                onChange={(val) => { setDepartmentFilter(val); setPage(0); }}
              />
            </div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-0">
          <DataTable columns={columns} data={doctors} loading={loading} emptyMessage="No doctors found matching criteria" />
        </div>
        <div className="card-footer bg-white border-0 px-3 py-2">
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>
    </div>
  );
};

export default DoctorListPage;
