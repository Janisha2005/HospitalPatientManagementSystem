import api from './api';

export const pharmacyService = {
  // Categories
  getCategories: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/categories', { params: { page, size } });
    return res.data;
  },
  getActiveCategories: async () => {
    const res = await api.get('/pharmacy/categories/active');
    return res.data;
  },
  createCategory: async (data) => {
    const res = await api.post('/pharmacy/categories', data);
    return res.data;
  },
  updateCategory: async (id, data) => {
    const res = await api.put(`/pharmacy/categories/${id}`, data);
    return res.data;
  },

  // Medicines
  getMedicines: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/medicines', { params: { page, size } });
    return res.data;
  },
  getActiveMedicines: async () => {
    const res = await api.get('/pharmacy/medicines/active');
    return res.data;
  },
  getMedicineById: async (id) => {
    const res = await api.get(`/pharmacy/medicines/${id}`);
    return res.data;
  },
  createMedicine: async (data) => {
    const res = await api.post('/pharmacy/medicines', data);
    return res.data;
  },
  updateMedicine: async (id, data) => {
    const res = await api.put(`/pharmacy/medicines/${id}`, data);
    return res.data;
  },

  // Suppliers
  getSuppliers: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/suppliers', { params: { page, size } });
    return res.data;
  },
  getActiveSuppliers: async () => {
    const res = await api.get('/pharmacy/suppliers/active');
    return res.data;
  },
  getSupplierById: async (id) => {
    const res = await api.get(`/pharmacy/suppliers/${id}`);
    return res.data;
  },
  createSupplier: async (data) => {
    const res = await api.post('/pharmacy/suppliers', data);
    return res.data;
  },
  updateSupplier: async (id, data) => {
    const res = await api.put(`/pharmacy/suppliers/${id}`, data);
    return res.data;
  },

  // Batches
  getBatches: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/batches', { params: { page, size } });
    return res.data;
  },
  getBatchesByMedicine: async (medicineId) => {
    const res = await api.get(`/pharmacy/batches/medicine/${medicineId}`);
    return res.data;
  },
  getNearExpiryBatches: async () => {
    const res = await api.get('/pharmacy/batches/near-expiry');
    return res.data;
  },
  getExpiredBatches: async () => {
    const res = await api.get('/pharmacy/batches/expired');
    return res.data;
  },
  createBatch: async (data) => {
    const res = await api.post('/pharmacy/batches', data);
    return res.data;
  },

  // Inventory & Stock Adjustment
  getInventory: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/inventory', { params: { page, size } });
    return res.data;
  },
  getLowStock: async () => {
    const res = await api.get('/pharmacy/inventory/low-stock');
    return res.data;
  },
  getMedicineTransactions: async (medicineId, page = 0, size = 20) => {
    const res = await api.get(`/pharmacy/inventory/${medicineId}/transactions`, { params: { page, size } });
    return res.data;
  },
  adjustStock: async (data) => {
    const res = await api.post('/pharmacy/inventory/adjust', data);
    return res.data;
  },

  // Purchase Orders
  getPurchaseOrders: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/purchase-orders', { params: { page, size } });
    return res.data;
  },
  getPurchaseOrderById: async (id) => {
    const res = await api.get(`/pharmacy/purchase-orders/${id}`);
    return res.data;
  },
  createPurchaseOrder: async (data) => {
    const res = await api.post('/pharmacy/purchase-orders', data);
    return res.data;
  },
  updatePurchaseOrderStatus: async (id, status) => {
    const res = await api.patch(`/pharmacy/purchase-orders/${id}/status`, null, { params: { status } });
    return res.data;
  },

  // Goods Receipts (GRN)
  getGoodsReceipts: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/goods-receipts', { params: { page, size } });
    return res.data;
  },
  getGoodsReceiptById: async (id) => {
    const res = await api.get(`/pharmacy/goods-receipts/${id}`);
    return res.data;
  },
  createGoodsReceipt: async (data) => {
    const res = await api.post('/pharmacy/goods-receipts', data);
    return res.data;
  },

  // Dispensing
  getDispensings: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/dispensing', { params: { page, size } });
    return res.data;
  },
  dispenseMedicine: async (data) => {
    const res = await api.post('/pharmacy/dispensing', data);
    return res.data;
  },
  getDispensingsByPrescription: async (prescriptionId) => {
    const res = await api.get(`/pharmacy/prescriptions/${prescriptionId}/dispensing`);
    return res.data;
  },

  // Returns
  getReturns: async (page = 0, size = 20) => {
    const res = await api.get('/pharmacy/returns', { params: { page, size } });
    return res.data;
  },
  processPatientReturn: async (data) => {
    const res = await api.post('/pharmacy/returns/patient', data);
    return res.data;
  },
  processSupplierReturn: async (data) => {
    const res = await api.post('/pharmacy/returns/supplier', data);
    return res.data;
  },

  // Dashboards
  getPharmacyDashboard: async () => {
    const res = await api.get('/pharmacy/dashboard');
    return res.data;
  },
  getInventoryDashboard: async () => {
    const res = await api.get('/pharmacy/inventory/dashboard');
    return res.data;
  }
};

export default pharmacyService;
