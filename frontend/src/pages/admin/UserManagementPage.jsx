import React, { useEffect, useState } from 'react';
import DataTable from '../../components/DataTable';
import StatusBadge from '../../components/StatusBadge';
import Pagination from '../../components/Pagination';
import Filter from '../../components/Filter';
import Modal from '../../components/Modal';
import FormInput from '../../components/FormInput';
import SelectInput from '../../components/SelectInput';
import ErrorMessage from '../../components/ErrorMessage';
import { userService } from '../../services/userService';

const UserManagementPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [roleFilter, setRoleFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [currentUserId, setCurrentUserId] = useState(null);

  // Password Reset Modal
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const [newPassword, setNewPassword] = useState('');

  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    fullName: '',
    phone: '',
    role: 'ADMIN',
    isActive: true
  });
  const [fieldErrors, setFieldErrors] = useState({});

  const roleOptions = [
    { label: 'All Roles', value: '' },
    { label: 'ADMIN', value: 'ADMIN' },
    { label: 'DOCTOR', value: 'DOCTOR' },
    { label: 'NURSE', value: 'NURSE' },
    { label: 'RECEPTIONIST', value: 'RECEPTIONIST' },
    { label: 'PATIENT', value: 'PATIENT' }
  ];

  const createRoleOptions = [
    { label: 'ADMIN', value: 'ADMIN' },
    { label: 'DOCTOR', value: 'DOCTOR' },
    { label: 'NURSE', value: 'NURSE' },
    { label: 'RECEPTIONIST', value: 'RECEPTIONIST' },
    { label: 'PATIENT', value: 'PATIENT' }
  ];

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await userService.getAllUsers({
        role: roleFilter,
        page,
        size: 10
      });
      if (response.success) {
        setUsers(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (err) {
      setError(err.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [roleFilter, page]);

  const handleOpenCreateModal = () => {
    setIsEditMode(false);
    setFormData({
      username: '',
      password: '',
      email: '',
      fullName: '',
      phone: '',
      role: 'ADMIN',
      isActive: true
    });
    setFieldErrors({});
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (user) => {
    setIsEditMode(true);
    setCurrentUserId(user.id);
    setFormData({
      username: user.username,
      password: '',
      email: user.email,
      fullName: user.fullName,
      phone: user.phone || '',
      role: user.role,
      isActive: user.isActive
    });
    setFieldErrors({});
    setIsModalOpen(true);
  };

  const handleOpenPasswordModal = (userId) => {
    setCurrentUserId(userId);
    setNewPassword('');
    setIsPasswordModalOpen(true);
  };

  const handleStatusToggle = async (userId, currentStatus) => {
    try {
      const response = await userService.updateUserStatus(userId, !currentStatus);
      if (response.success) {
        setSuccess(`User status updated to ${!currentStatus ? 'ACTIVE' : 'INACTIVE'}`);
        fetchUsers();
      }
    } catch (err) {
      setError(err.message || 'Failed to update user status');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFieldErrors({});
    setError('');

    try {
      if (isEditMode) {
        const response = await userService.updateUser(currentUserId, {
          email: formData.email,
          fullName: formData.fullName,
          phone: formData.phone,
          role: formData.role,
          isActive: formData.isActive
        });
        if (response.success) {
          setSuccess('User updated successfully');
          setIsModalOpen(false);
          fetchUsers();
        }
      } else {
        const response = await userService.createUser(formData);
        if (response.success) {
          setSuccess('User created successfully');
          setIsModalOpen(false);
          fetchUsers();
        }
      }
    } catch (err) {
      if (err.errors && typeof err.errors === 'object') {
        setFieldErrors(err.errors);
      } else {
        setError(err.message || 'Validation error');
      }
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    try {
      const response = await userService.resetPassword(currentUserId, newPassword);
      if (response.success) {
        setSuccess('Password reset successfully');
        setIsPasswordModalOpen(false);
      }
    } catch (err) {
      setError(err.message || 'Failed to reset password');
    }
  };

  const columns = [
    { header: 'Name', cell: (row) => <span className="fw-semibold">{row.fullName}</span> },
    { header: 'Username', accessor: 'username' },
    { header: 'Email', accessor: 'email' },
    {
      header: 'Role',
      cell: (row) => <span className="badge bg-secondary font-monospace">{row.role}</span>
    },
    {
      header: 'Status',
      cell: (row) => <StatusBadge isActive={row.isActive} />
    },
    {
      header: 'Created Date',
      cell: (row) => row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '-'
    },
    {
      header: 'Actions',
      cell: (row) => (
        <div className="btn-group btn-group-sm">
          <button className="btn btn-outline-primary" onClick={() => handleOpenEditModal(row)} title="Edit User">
            <i className="bi bi-pencil"></i>
          </button>
          <button className="btn btn-outline-warning" onClick={() => handleOpenPasswordModal(row.id)} title="Reset Password">
            <i className="bi bi-key"></i>
          </button>
          <button
            className={`btn ${row.isActive ? 'btn-outline-danger' : 'btn-outline-success'}`}
            onClick={() => handleStatusToggle(row.id, row.isActive)}
            title={row.isActive ? 'Deactivate' : 'Activate'}
          >
            <i className={`bi ${row.isActive ? 'bi-person-x' : 'bi-person-check'}`}></i>
          </button>
        </div>
      )
    }
  ];

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold mb-0 text-dark">User Management</h3>
          <p className="text-muted small mb-0">System user accounts, role permissions, and access controls</p>
        </div>
        <button className="btn btn-primary" onClick={handleOpenCreateModal}>
          <i className="bi bi-person-plus me-1"></i> Create User
        </button>
      </div>

      {success && (
        <div className="alert alert-success alert-dismissible fade show" role="alert">
          <i className="bi bi-check-circle me-2"></i>{success}
          <button type="button" className="btn-close" onClick={() => setSuccess('')}></button>
        </div>
      )}
      <ErrorMessage message={error} onClose={() => setError('')} />

      <div className="card border-0 shadow-sm rounded-3 mb-4">
        <div className="card-body p-3">
          <div className="row g-2 align-items-center">
            <div className="col-12 col-md-4">
              <Filter
                label="Role Filter"
                options={roleOptions}
                value={roleFilter}
                onChange={(val) => { setRoleFilter(val); setPage(0); }}
              />
            </div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-0">
          <DataTable columns={columns} data={users} loading={loading} emptyMessage="No users found" />
        </div>
        <div className="card-footer bg-white border-0 px-3 py-2">
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>

      {/* User Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditMode ? 'Edit User' : 'Create User'}
      >
        <form onSubmit={handleSubmit}>
          {!isEditMode && (
            <FormInput
              label="Username"
              name="username"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              error={fieldErrors.username}
              required
            />
          )}

          {!isEditMode && (
            <FormInput
              label="Password"
              name="password"
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              error={fieldErrors.password}
              required
            />
          )}

          <FormInput
            label="Full Name"
            name="fullName"
            value={formData.fullName}
            onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
            error={fieldErrors.fullName}
            required
          />

          <FormInput
            label="Email"
            name="email"
            type="email"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            error={fieldErrors.email}
            required
          />

          <FormInput
            label="Phone"
            name="phone"
            value={formData.phone}
            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            error={fieldErrors.phone}
          />

          <SelectInput
            label="Role"
            name="role"
            value={formData.role}
            onChange={(e) => setFormData({ ...formData, role: e.target.value })}
            options={createRoleOptions}
            error={fieldErrors.role}
            required
          />

          <div className="d-flex justify-content-end gap-2 mt-4">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {isEditMode ? 'Save Changes' : 'Create User'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Password Reset Modal */}
      <Modal
        isOpen={isPasswordModalOpen}
        onClose={() => setIsPasswordModalOpen(false)}
        title="Reset Password"
      >
        <form onSubmit={handleResetPassword}>
          <FormInput
            label="New Password"
            name="newPassword"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
          />
          <div className="d-flex justify-content-end gap-2 mt-4">
            <button type="button" className="btn btn-secondary" onClick={() => setIsPasswordModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-warning">
              Reset Password
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default UserManagementPage;
