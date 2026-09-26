import api from './api';

const dischargeService = {
  getDischargePlan: async (admissionId) => {
    const response = await api.get(`/ipd/admissions/${admissionId}/discharge-plan`);
    return response.data;
  },

  createDischargePlan: async (admissionId, data) => {
    const response = await api.post(`/ipd/admissions/${admissionId}/discharge-plan`, data);
    return response.data;
  },

  getDischargeSummary: async (admissionId) => {
    const response = await api.get(`/ipd/admissions/${admissionId}/discharge-summary`);
    return response.data;
  },

  createDischargeSummary: async (admissionId, data) => {
    const response = await api.post(`/ipd/admissions/${admissionId}/discharge-summary`, data);
    return response.data;
  },

  dischargePatient: async (admissionId, data) => {
    const response = await api.post(`/ipd/admissions/${admissionId}/discharge`, data);
    return response.data;
  }
};

export default dischargeService;
