import api from './api';

const bedService = {
  getAllBeds: async (wardId, bedType, status) => {
    const params = {};
    if (wardId) params.wardId = wardId;
    if (bedType) params.bedType = bedType;
    if (status) params.status = status;
    const response = await api.get('/beds', { params });
    return response.data;
  },

  getAvailableBeds: async (wardId, bedType) => {
    const params = {};
    if (wardId) params.wardId = wardId;
    if (bedType) params.bedType = bedType;
    const response = await api.get('/beds/available', { params });
    return response.data;
  },

  getBedsByWard: async (wardId) => {
    const response = await api.get(`/wards/${wardId}/beds`);
    return response.data;
  },

  getBedById: async (id) => {
    const response = await api.get(`/beds/${id}`);
    return response.data;
  },

  createBed: async (data) => {
    const response = await api.post('/beds', data);
    return response.data;
  },

  updateBed: async (id, data) => {
    const response = await api.put(`/beds/${id}`, data);
    return response.data;
  },

  updateBedStatus: async (id, status) => {
    const response = await api.patch(`/beds/${id}/status`, { status });
    return response.data;
  }
};

export default bedService;
