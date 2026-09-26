import api from './api';

export const medicineService = {
  getAllMedicines: async (activeOnly = true) => {
    const response = await api.get('/medicines', { params: { activeOnly } });
    return response.data;
  },

  searchMedicines: async (query) => {
    const response = await api.get('/medicines/search', { params: { query } });
    return response.data;
  },

  getMedicineById: async (id) => {
    const response = await api.get(`/medicines/${id}`);
    return response.data;
  },

  createMedicine: async (medicineData) => {
    const response = await api.post('/medicines', medicineData);
    return response.data;
  },

  updateMedicine: async (id, medicineData) => {
    const response = await api.put(`/medicines/${id}`, medicineData);
    return response.data;
  },
};

export default medicineService;
