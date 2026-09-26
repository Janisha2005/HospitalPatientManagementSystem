import api from './api';

export const prescriptionService = {
  getAllPrescriptions: async (status = null) => {
    const response = await api.get('/prescriptions', { params: { status } });
    return response.data;
  },

  getPatientPrescriptions: async (patientId) => {
    const response = await api.get(`/patients/${patientId}/prescriptions`);
    return response.data;
  },

  getPrescriptionById: async (id) => {
    const response = await api.get(`/prescriptions/${id}`);
    return response.data;
  },

  createPrescription: async (prescriptionData) => {
    const response = await api.post('/prescriptions', prescriptionData);
    return response.data;
  },

  addItemToPrescription: async (prescriptionId, itemData) => {
    const response = await api.post(`/prescriptions/${prescriptionId}/items`, itemData);
    return response.data;
  },

  removeItemFromPrescription: async (prescriptionId, itemId) => {
    const response = await api.delete(`/prescriptions/${prescriptionId}/items/${itemId}`);
    return response.data;
  },

  issuePrescription: async (prescriptionId) => {
    const response = await api.put(`/prescriptions/${prescriptionId}/issue`);
    return response.data;
  },

  cancelPrescription: async (prescriptionId, reason = '') => {
    const response = await api.put(`/prescriptions/${prescriptionId}/cancel`, null, {
      params: { reason },
    });
    return response.data;
  },
};

export default prescriptionService;
