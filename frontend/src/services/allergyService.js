import api from './api';

export const allergyService = {
  getPatientAllergies: async (patientId, activeOnly = false) => {
    const response = await api.get(`/patients/${patientId}/allergies`, {
      params: { activeOnly },
    });
    return response.data;
  },

  addAllergy: async (patientId, allergyData) => {
    const response = await api.post(`/patients/${patientId}/allergies`, allergyData);
    return response.data;
  },

  updateAllergy: async (patientId, allergyId, allergyData) => {
    const response = await api.put(`/patients/${patientId}/allergies/${allergyId}`, allergyData);
    return response.data;
  },

  toggleAllergyStatus: async (patientId, allergyId) => {
    const response = await api.patch(`/patients/${patientId}/allergies/${allergyId}/status`);
    return response.data;
  },
};

export default allergyService;
