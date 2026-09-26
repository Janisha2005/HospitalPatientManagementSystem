import api from './api';

export const opdService = {
  getVisits(params = {}) {
    return api.get('/opd/visits', { params });
  },

  getVisitById(id) {
    return api.get(`/opd/visits/${id}`);
  },

  getVisitByCode(opdVisitId) {
    return api.get(`/opd/visits/code/${opdVisitId}`);
  },

  updateVisitDetails(id, visitData) {
    return api.put(`/opd/visits/${id}`, visitData);
  },

  updateStatus(id, status) {
    return api.patch(`/opd/visits/${id}/status`, { status });
  },

  getQueue(params = {}) {
    return api.get('/opd/queue', { params });
  },

  getTodayQueue(params = {}) {
    return api.get('/opd/queue/today', { params });
  },

  callQueuePatient(id) {
    return api.post(`/opd/queue/${id}/call`);
  },

  startConsultation(id) {
    return api.post(`/opd/queue/${id}/start`);
  },

  completeConsultation(id, visitData = null) {
    return api.post(`/opd/queue/${id}/complete`, visitData);
  },

  getDashboardStats(doctorId = null) {
    const params = doctorId ? { doctorId } : {};
    return api.get('/opd/dashboard', { params });
  }
};
