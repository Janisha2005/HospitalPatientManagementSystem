import api from './api';

export const authService = {
  async login(username, password) {
    const response = await api.post('/auth/login', { username, password });
    if (response.success && response.data?.accessToken) {
      localStorage.setItem('token', response.data.accessToken);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
    return response;
  },

  async getCurrentUser() {
    const response = await api.get('/auth/me');
    if (response.success && response.data) {
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response;
  },

  async logout() {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // Ignore API failure on logout
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  },

  getStoredToken() {
    return localStorage.getItem('token');
  },

  getStoredUser() {
    const userStr = localStorage.getItem('user');
    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  },

  isAuthenticated() {
    return !!localStorage.getItem('token');
  },

  hasRole(...roles) {
    const user = this.getStoredUser();
    if (!user || !user.role) return false;
    return roles.includes(user.role);
  }
};
