import api from './api';

export const doctorService = {
  searchDoctors(params = {}) {
    return api.get('/doctors', { params });
  },

  getDoctorById(id) {
    return api.get(`/doctors/${id}`);
  },

  createDoctor(doctorData) {
    return api.post('/doctors', doctorData);
  },

  updateDoctor(id, doctorData) {
    return api.put(`/doctors/${id}`, doctorData);
  },

  updateDoctorStatus(id, isActive) {
    return api.patch(`/doctors/${id}/status`, { isActive });
  }
};
