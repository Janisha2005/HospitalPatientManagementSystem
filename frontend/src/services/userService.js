import api from './api';

export const userService = {
  getAllUsers(params = {}) {
    return api.get('/users', { params });
  },

  getUserById(id) {
    return api.get(`/users/${id}`);
  },

  createUser(userData) {
    return api.post('/users', userData);
  },

  updateUser(id, userData) {
    return api.put(`/users/${id}`, userData);
  },

  updateUserStatus(id, isActive) {
    return api.patch(`/users/${id}/status`, { isActive });
  },

  resetPassword(id, newPassword) {
    return api.patch(`/users/${id}/password`, { newPassword });
  }
};
