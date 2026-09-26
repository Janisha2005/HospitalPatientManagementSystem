import api from './api';

const nursingService = {
  getNursingDashboard: async () => {
    const response = await api.get('/ipd/nursing/dashboard');
    return response.data;
  },

  getVitalsForAdmission: async (admissionId) => {
    const response = await api.get(`/ipd/admissions/${admissionId}/vitals`);
    return response.data;
  },

  recordVitals: async (admissionId, data) => {
    const response = await api.post(`/ipd/admissions/${admissionId}/vitals`, data);
    return response.data;
  },

  getNursingNotesForAdmission: async (admissionId) => {
    const response = await api.get(`/ipd/admissions/${admissionId}/nursing-notes`);
    return response.data;
  },

  createNursingNote: async (admissionId, data) => {
    const response = await api.post(`/ipd/admissions/${admissionId}/nursing-notes`, data);
    return response.data;
  },

  updateNursingNote: async (id, data) => {
    const response = await api.put(`/ipd/nursing-notes/${id}`, data);
    return response.data;
  }
};

export default nursingService;
