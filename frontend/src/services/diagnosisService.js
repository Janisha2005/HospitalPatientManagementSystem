import api from './api';

export const diagnosisService = {
  getPatientDiagnoses: async (patientId) => {
    const response = await api.get(`/patients/${patientId}/diagnoses`);
    return response.data;
  },

  getVisitDiagnoses: async (opdVisitId) => {
    const response = await api.get(`/opd/visits/${opdVisitId}/diagnoses`);
    return response.data;
  },

  createDiagnosis: async (patientId, diagnosisData) => {
    const response = await api.post(`/patients/${patientId}/diagnoses`, diagnosisData);
    return response.data;
  },
};

export default diagnosisService;
