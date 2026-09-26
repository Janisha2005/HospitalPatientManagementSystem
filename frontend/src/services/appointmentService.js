import api from './api';

export const appointmentService = {
  getAppointments(params = {}) {
    return api.get('/appointments', { params });
  },

  getAppointmentById(id) {
    return api.get(`/appointments/${id}`);
  },

  getAppointmentByCode(appointmentId) {
    return api.get(`/appointments/code/${appointmentId}`);
  },

  createAppointment(appointmentData) {
    return api.post('/appointments', appointmentData);
  },

  updateAppointment(id, appointmentData) {
    return api.put(`/appointments/${id}`, appointmentData);
  },

  updateStatus(id, status, notes = '') {
    return api.patch(`/appointments/${id}/status`, { status, notes });
  },

  confirmAppointment(id) {
    return api.post(`/appointments/${id}/confirm`);
  },

  cancelAppointment(id, reason = 'Cancelled by user') {
    return api.post(`/appointments/${id}/cancel`, null, { params: { reason } });
  },

  checkInPatient(id) {
    return api.post(`/appointments/${id}/check-in`);
  },

  rescheduleAppointment(id, date, startTime, endTime) {
    return api.post(`/appointments/${id}/reschedule`, null, {
      params: { date, startTime, endTime }
    });
  }
};
