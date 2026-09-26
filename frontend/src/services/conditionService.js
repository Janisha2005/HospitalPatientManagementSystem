import api from './api';

export const conditionService = {
  getPatientConditions: async (patientId, activeOnly = false) => {
    const response = await api.get(`/patients/${patientId}/conditions`, {
      params: { activeOnly },
    });
    return response.data;
  },

  addCondition: async (patientId, conditionData) => {
    const response = await api.post(`/patients/${patientId}/conditions`, conditionData);
    return response.data;
  },

  updateCondition: async (patientId, conditionId, conditionData) => {
    const response = await api.put(`/patients/${patientId}/conditions/${conditionId}`, conditionData);
    return response.data;
  },

  toggleConditionStatus: async (patientId, conditionId) => {
    const response = await api.patch(`/patients/${patientId}/conditions/${conditionId}/status`);
    return response.data;
  },
};

export default conditionService;
