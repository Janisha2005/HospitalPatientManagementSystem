import api from './api';

export const patientService = {
  searchPatients(params = {}) {
    return api.get('/patients', { params });
  },

  getPatientById(id) {
    return api.get(`/patients/${id}`);
  },

  getPatientByPatientId(patientId) {
    return api.get(`/patients/code/${patientId}`);
  },

  createPatient(patientData) {
    return api.post('/patients', patientData);
  },

  updatePatient(id, patientData) {
    return api.put(`/patients/${id}`, patientData);
  },

  getAllPatients(page = 0, size = 100) {
    return api.get('/patients', { params: { page, size } });
  },

  updatePatientStatus(id, isActive) {
    return api.patch(`/patients/${id}/status`, { isActive });
  }
};

export default patientService;
