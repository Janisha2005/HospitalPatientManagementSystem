import React, { useEffect, useState } from 'react';
import DataTable from '../../components/DataTable';
import StatusBadge from '../../components/StatusBadge';
import Pagination from '../../components/Pagination';
import Modal from '../../components/Modal';
import FormInput from '../../components/FormInput';
import ErrorMessage from '../../components/ErrorMessage';
import ConfirmationDialog from '../../components/ConfirmationDialog';
import { departmentService } from '../../services/departmentService';

const DepartmentManagementPage = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [currentDeptId, setCurrentDeptId] = useState(null);

  // Delete Confirmation State
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);

  const [formData, setFormData] = useState({
    departmentCode: '',
    departmentName: '',
    description: '',
    location: '',
    isActive: true
  });
  const [fieldErrors, setFieldErrors] = useState({});

  const fetchDepartments = async () => {
    setLoading(true);
    try {
      const response = await departmentService.getAllDepartments({ page, size: 10 });
      if (response.success) {
        setDepartments(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (err) {
      setError(err.message || 'Failed to load departments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDepartments();
  }, [page]);

  const handleOpenCreateModal = () => {
    setIsEditMode(false);
    setFormData({
      departmentCode: '',
      departmentName: '',
      description: '',
      location: '',
      isActive: true
    });
    setFieldErrors({});
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (dept) => {
    setIsEditMode(true);
    setCurrentDeptId(dept.id);
    setFormData({
      departmentCode: dept.departmentCode,
      departmentName: dept.departmentName,
      description: dept.description || '',
      location: dept.location || '',
      isActive: dept.isActive
    });
    setFieldErrors({});
    setIsModalOpen(true);
  };

  const handleStatusToggle = async (deptId, currentStatus) => {
    try {
      const response = await departmentService.updateDepartmentStatus(deptId, !currentStatus);
      if (response.success) {
        setSuccess(`Department status updated to ${!currentStatus ? 'ACTIVE' : 'INACTIVE'}`);
        fetchDepartments();
      }
    } catch (err) {
      setError(err.message || 'Failed to update department status');
    }
  };

  const handleDelete = async () => {
    if (!deleteConfirmId) return;
    try {
      const response = await departmentService.deleteDepartment(deleteConfirmId);
      if (response.success) {
        setSuccess('Department deleted successfully');
        setDeleteConfirmId(null);
        fetchDepartments();
      }
    } catch (err) {
      setError(err.message || 'Cannot delete department');
      setDeleteConfirmId(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFieldErrors({});
    setError('');

    try {
      if (isEditMode) {
        const response = await departmentService.updateDepartment(currentDeptId, formData);
        if (response.success) {
          setSuccess('Department updated successfully');
          setIsModalOpen(false);
          fetchDepartments();
        }
      } else {
        const response = await departmentService.createDepartment(formData);
        if (response.success) {
          setSuccess('Department created successfully');
          setIsModalOpen(false);
          fetchDepartments();
        }
      }
    } catch (err) {
      if (err.errors && typeof err.errors === 'object') {
        setFieldErrors(err.errors);
      } else {
        setError(err.message || 'Validation failed');
      }
    }
  };

  const columns = [
    {
      header: 'Code',
      cell: (row) => <span className="badge bg-primary font-monospace">{row.departmentCode}</span>
    },
    { header: 'Department Name', cell: (row) => <span className="fw-semibold">{row.departmentName}</span> },
    { header: 'Location', accessor: 'location' },
    {
      header: 'Doctors Assigned',
      cell: (row) => <span className="badge bg-light text-dark border">{row.doctorCount || 0}</span>
    },
    {
      header: 'Status',
      cell: (row) => <StatusBadge isActive={row.isActive} />
    },
    {
      header: 'Actions',
      cell: (row) => (
        <div className="btn-group btn-group-sm">
          <button className="btn btn-outline-primary" onClick={() => handleOpenEditModal(row)} title="Edit">
            <i className="bi bi-pencil"></i>
          </button>
          <button
            className={`btn ${row.isActive ? 'btn-outline-warning' : 'btn-outline-success'}`}
            onClick={() => handleStatusToggle(row.id, row.isActive)}
            title={row.isActive ? 'Deactivate' : 'Activate'}
          >
            <i className={`bi ${row.isActive ? 'bi-slash-circle' : 'bi-check-circle'}`}></i>
          </button>
          <button
            className="btn btn-outline-danger"
            onClick={() => setDeleteConfirmId(row.id)}
            title="Delete Safely"
          >
            <i className="bi bi-trash"></i>
          </button>
        </div>
      )
    }
  ];

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold mb-0 text-dark">Department Management</h3>
          <p className="text-muted small mb-0">Manage hospital clinical & specialized departments</p>
        </div>
        <button className="btn btn-primary" onClick={handleOpenCreateModal}>
          <i className="bi bi-plus-circle me-1"></i> Add Department
        </button>
      </div>

      {success && (
        <div className="alert alert-success alert-dismissible fade show" role="alert">
          <i className="bi bi-check-circle me-2"></i>{success}
          <button type="button" className="btn-close" onClick={() => setSuccess('')}></button>
        </div>
      )}
      <ErrorMessage message={error} onClose={() => setError('')} />

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-0">
          <DataTable columns={columns} data={departments} loading={loading} emptyMessage="No departments found" />
        </div>
        <div className="card-footer bg-white border-0 px-3 py-2">
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditMode ? 'Edit Department' : 'Add Department'}
      >
        <form onSubmit={handleSubmit}>
          <FormInput
            label="Department Code"
            name="departmentCode"
            value={formData.departmentCode}
            onChange={(e) => setFormData({ ...formData, departmentCode: e.target.value.toUpperCase() })}
            error={fieldErrors.departmentCode}
            placeholder="e.g. CARDIO, ORTHO"
            required
          />

          <FormInput
            label="Department Name"
            name="departmentName"
            value={formData.departmentName}
            onChange={(e) => setFormData({ ...formData, departmentName: e.target.value })}
            error={fieldErrors.departmentName}
            placeholder="e.g. Cardiology"
            required
          />

          <FormInput
            label="Location"
            name="location"
            value={formData.location}
            onChange={(e) => setFormData({ ...formData, location: e.target.value })}
            error={fieldErrors.location}
            placeholder="e.g. Building A, 3rd Floor"
          />

          <div className="mb-3">
            <label className="form-label fw-semibold small">Description</label>
            <textarea
              className="form-control"
              rows="3"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Department description..."
            ></textarea>
          </div>

          <div className="d-flex justify-content-end gap-2 mt-4">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {isEditMode ? 'Save Changes' : 'Add Department'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmationDialog
        isOpen={!!deleteConfirmId}
        onClose={() => setDeleteConfirmId(null)}
        onConfirm={handleDelete}
        title="Delete Department"
        message="Are you sure you want to delete this department? Note: Departments with assigned doctors cannot be deleted."
      />
    </div>
  );
};

export default DepartmentManagementPage;
