import api from './api';

const ipdService = {
  getDashboardMetrics: async () => {
    const response = await api.get('/ipd/dashboard');
    return response.data;
  },

  getAllAdmissions: async (filters = {}, page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') => {
    const params = { page, size, sortBy, sortDir, ...filters };
    const response = await api.get('/ipd/admissions', { params });
    return response.data;
  },

  getAdmissionById: async (id) => {
    const response = await api.get(`/ipd/admissions/${id}`);
    return response.data;
  },

  getAdmissionByCode: async (code) => {
    const response = await api.get(`/ipd/admissions/code/${code}`);
    return response.data;
  },

  createAdmission: async (data) => {
    const response = await api.post('/ipd/admissions', data);
    return response.data;
  },

  approveAdmission: async (id) => {
    const response = await api.post(`/ipd/admissions/${id}/approve`);
    return response.data;
  },

  admitPatient: async (id, wardId, bedId) => {
    const response = await api.post(`/ipd/admissions/${id}/admit`, { wardId, bedId });
    return response.data;
  },

  cancelAdmission: async (id, reason) => {
    const response = await api.post(`/ipd/admissions/${id}/cancel`, { reason });
    return response.data;
  },

  allocateBed: async (admissionId, wardId, bedId, allocationType, reason) => {
    const response = await api.post(`/ipd/admissions/${admissionId}/allocate-bed`, {
      wardId,
      bedId,
      allocationType,
      reason
    });
    return response.data;
  },

  transferPatient: async (admissionId, toWardId, toBedId, reason) => {
    const response = await api.post(`/ipd/admissions/${admissionId}/transfer`, {
      toWardId,
      toBedId,
      reason
    });
    return response.data;
  },

  getBedHistory: async (admissionId) => {
    const response = await api.get(`/ipd/admissions/${admissionId}/bed-history`);
    return response.data;
  },

  getTransferHistory: async (admissionId) => {
    const response = await api.get(`/ipd/admissions/${admissionId}/transfers`);
    return response.data;
  }
};

export default ipdService;
