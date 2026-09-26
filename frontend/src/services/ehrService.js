import api from './api';

export const ehrService = {
  getPatientEhrRecords: async (patientId) => {
    const response = await api.get(`/ehr/patients/${patientId}`);
    return response.data;
  },

  getPatientClinicalTimeline: async (patientId) => {
    const response = await api.get(`/ehr/patients/${patientId}/timeline`);
    return response.data;
  },

  createEhrRecord: async (ehrData) => {
    const response = await api.post('/ehr/records', ehrData);
    return response.data;
  },
};

export default ehrService;
