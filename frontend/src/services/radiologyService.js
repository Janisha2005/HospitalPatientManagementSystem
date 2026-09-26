import api from './api';

export const radiologyService = {
  // Catalog
  getAllRadiologyTests: async (activeOnly = false) => {
    const response = await api.get('/radiology/tests', { params: { activeOnly } });
    return response.data;
  },

  getRadiologyTestById: async (id) => {
    const response = await api.get(`/radiology/tests/${id}`);
    return response.data;
  },

  createRadiologyTest: async (testData) => {
    const response = await api.post('/radiology/tests', testData);
    return response.data;
  },

  updateRadiologyTest: async (id, testData) => {
    const response = await api.put(`/radiology/tests/${id}`, testData);
    return response.data;
  },

  // Orders
  getAllOrders: async (status = null) => {
    const response = await api.get('/radiology/orders', { params: { status } });
    return response.data;
  },

  getPatientOrders: async (patientId) => {
    const response = await api.get(`/radiology/patients/${patientId}/orders`);
    return response.data;
  },

  getOrderById: async (id) => {
    const response = await api.get(`/radiology/orders/${id}`);
    return response.data;
  },

  createOrder: async (orderData) => {
    const response = await api.post('/radiology/orders', orderData);
    return response.data;
  },

  updateOrderStatus: async (orderId, status) => {
    const response = await api.put(`/radiology/orders/${orderId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  // Reports
  getReportByOrderId: async (orderId) => {
    const response = await api.get(`/radiology/orders/${orderId}/report`);
    return response.data;
  },

  createOrUpdateReport: async (orderId, reportData) => {
    const response = await api.post(`/radiology/orders/${orderId}/report`, reportData);
    return response.data;
  },

  getDashboardStats: async () => {
    const response = await api.get('/radiology/dashboard');
    return response.data;
  },
};

export default radiologyService;
