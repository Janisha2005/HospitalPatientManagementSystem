import api from './api';

export const laboratoryService = {
  // Tests Catalog
  getAllLabTests: async (activeOnly = false) => {
    const response = await api.get('/lab/tests', { params: { activeOnly } });
    return response.data;
  },

  getLabTestById: async (id) => {
    const response = await api.get(`/lab/tests/${id}`);
    return response.data;
  },

  createLabTest: async (testData) => {
    const response = await api.post('/lab/tests', testData);
    return response.data;
  },

  updateLabTest: async (id, testData) => {
    const response = await api.put(`/lab/tests/${id}`, testData);
    return response.data;
  },

  // Orders
  getAllOrders: async (status = null) => {
    const response = await api.get('/lab/orders', { params: { status } });
    return response.data;
  },

  getPatientOrders: async (patientId) => {
    const response = await api.get(`/lab/patients/${patientId}/orders`);
    return response.data;
  },

  getOrderById: async (id) => {
    const response = await api.get(`/lab/orders/${id}`);
    return response.data;
  },

  createOrder: async (orderData) => {
    const response = await api.post('/lab/orders', orderData);
    return response.data;
  },

  collectSample: async (orderId) => {
    const response = await api.put(`/lab/orders/${orderId}/collect-sample`);
    return response.data;
  },

  enterResult: async (orderId, itemId, resultData) => {
    const response = await api.put(`/lab/orders/${orderId}/items/${itemId}/result`, resultData);
    return response.data;
  },

  verifyOrder: async (orderId) => {
    const response = await api.put(`/lab/orders/${orderId}/verify`);
    return response.data;
  },

  cancelOrder: async (orderId, reason = '') => {
    const response = await api.put(`/lab/orders/${orderId}/cancel`, null, {
      params: { reason },
    });
    return response.data;
  },

  getDashboardStats: async () => {
    const response = await api.get('/lab/dashboard');
    return response.data;
  },
};

export default laboratoryService;
