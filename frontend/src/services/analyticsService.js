import api from './api';

export const analyticsService = {

  // Executive Dashboard Analytics
  async getExecutiveAnalytics(params = {}) {
    return await api.get('/analytics/dashboard', { params });
  },

  // OPD Analytics
  async getOPDAnalytics(params = {}) {
    return await api.get('/analytics/opd', { params });
  },

  // IPD Analytics
  async getIPDAnalytics(params = {}) {
    return await api.get('/analytics/ipd', { params });
  },

  // Bed Occupancy Report
  async getBedOccupancyReport() {
    return await api.get('/analytics/beds');
  },

  // Pharmacy Analytics
  async getPharmacyAnalytics(params = {}) {
    return await api.get('/analytics/pharmacy', { params });
  },

  // Laboratory Analytics
  async getLaboratoryAnalytics(params = {}) {
    return await api.get('/analytics/laboratory', { params });
  },

  // Radiology Analytics
  async getRadiologyAnalytics(params = {}) {
    return await api.get('/analytics/radiology', { params });
  },

  // Patient Analytics
  async getPatientAnalytics(params = {}) {
    return await api.get('/analytics/patients', { params });
  },

  // Payment Collection Report
  async getPaymentCollectionReport(params = {}) {
    return await api.get('/reports/daily-collection', { params });
  },

  // Outstanding / Receivable Report
  async getOutstandingReport() {
    return await api.get('/reports/outstanding');
  },

  // Operational Doctor & Department Report
  async getOperationalReport(params = {}) {
    return await api.get('/reports/operational', { params });
  },

  // CSV Export URL builder
  getCSVExportUrl(reportType = 'OPERATIONAL', period = 'TODAY', fromDate = '', toDate = '') {
    let url = `/api/reports/export/csv?reportType=${reportType}&period=${period}`;
    if (fromDate) url += `&fromDate=${fromDate}`;
    if (toDate) url += `&toDate=${toDate}`;
    return url;
  }
};
