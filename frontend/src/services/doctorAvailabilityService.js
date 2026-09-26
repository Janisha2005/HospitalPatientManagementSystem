import api from './api';

export const doctorAvailabilityService = {
  getDoctorAvailability(doctorId) {
    return api.get(`/doctors/${doctorId}/availability`);
  },

  createAvailability(doctorId, availabilityData) {
    return api.post(`/doctors/${doctorId}/availability`, availabilityData);
  },

  updateAvailability(doctorId, availabilityId, availabilityData) {
    return api.put(`/doctors/${doctorId}/availability/${availabilityId}`, availabilityData);
  },

  deleteAvailability(doctorId, availabilityId) {
    return api.delete(`/doctors/${doctorId}/availability/${availabilityId}`);
  },

  getAvailableSlots(doctorId, date) {
    return api.get(`/doctors/${doctorId}/available-slots`, {
      params: { date }
    });
  }
};
