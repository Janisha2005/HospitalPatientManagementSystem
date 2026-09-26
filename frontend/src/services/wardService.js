import api from './api';

const wardService = {
  getAllWards: async (departmentId, activeOnly) => {
    const params = {};
    if (departmentId) params.departmentId = departmentId;
    if (activeOnly !== undefined) params.activeOnly = activeOnly;
    const response = await api.get('/wards', { params });
    return response.data;
  },

  getWardById: async (id) => {
    const response = await api.get(`/wards/${id}`);
    return response.data;
  },

  createWard: async (data) => {
    const response = await api.post('/wards', data);
    return response.data;
  },

  updateWard: async (id, data) => {
    const response = await api.put(`/wards/${id}`, data);
    return response.data;
  },

  updateWardStatus: async (id, isActive) => {
    const response = await api.patch(`/wards/${id}/status`, { isActive });
    return response.data;
  },

  deleteWard: async (id) => {
    const response = await api.delete(`/wards/${id}`);
    return response.data;
  }
};

export default wardService;
