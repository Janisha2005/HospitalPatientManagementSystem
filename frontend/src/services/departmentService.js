import api from './api';

export const departmentService = {
  getAllDepartments(params = {}) {
    return api.get('/departments', { params });
  },

  getAllActiveDepartments() {
    return api.get('/departments/active');
  },

  getDepartmentById(id) {
    return api.get(`/departments/${id}`);
  },

  createDepartment(deptData) {
    return api.post('/departments', deptData);
  },

  updateDepartment(id, deptData) {
    return api.put(`/departments/${id}`, deptData);
  },

  updateDepartmentStatus(id, isActive) {
    return api.patch(`/departments/${id}/status`, { isActive });
  },

  deleteDepartment(id) {
    return api.delete(`/departments/${id}`);
  }
};
