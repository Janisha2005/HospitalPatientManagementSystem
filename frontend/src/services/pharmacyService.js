import api from './api';

export const pharmacyService = {
  // Categories
  getCategories: (page = 0, size = 20) => api.get('/pharmacy/categories', { params: { page, size } }),
  getActiveCategories: () => api.get('/pharmacy/categories/active'),
  createCategory: (data) => api.post('/pharmacy/categories', data),
  updateCategory: (id, data) => api.put(`/pharmacy/categories/${id}`, data),

  // Medicines
  getMedicines: (page = 0, size = 20) => api.get('/pharmacy/medicines', { params: { page, size } }),
  getActiveMedicines: () => api.get('/pharmacy/medicines/active'),
  getMedicineById: (id) => api.get(`/pharmacy/medicines/${id}`),
  createMedicine: (data) => api.post('/pharmacy/medicines', data),
  updateMedicine: (id, data) => api.put(`/pharmacy/medicines/${id}`, data),

  // Suppliers
  getSuppliers: (page = 0, size = 20) => api.get('/pharmacy/suppliers', { params: { page, size } }),
  getActiveSuppliers: () => api.get('/pharmacy/suppliers/active'),
  getSupplierById: (id) => api.get(`/pharmacy/suppliers/${id}`),
  createSupplier: (data) => api.post('/pharmacy/suppliers', data),
  updateSupplier: (id, data) => api.put(`/pharmacy/suppliers/${id}`, data),

  // Batches
  getBatches: (page = 0, size = 20) => api.get('/pharmacy/batches', { params: { page, size } }),
  getBatchesByMedicine: (medicineId) => api.get(`/pharmacy/batches/medicine/${medicineId}`),
  getNearExpiryBatches: () => api.get('/pharmacy/batches/near-expiry'),
  getExpiredBatches: () => api.get('/pharmacy/batches/expired'),
  createBatch: (data) => api.post('/pharmacy/batches', data),

  // Inventory & Stock Adjustment
  getInventory: (page = 0, size = 20) => api.get('/pharmacy/inventory', { params: { page, size } }),
  getLowStock: () => api.get('/pharmacy/inventory/low-stock'),
  getMedicineTransactions: (medicineId, page = 0, size = 20) => api.get(`/pharmacy/inventory/${medicineId}/transactions`, { params: { page, size } }),
  adjustStock: (data) => api.post('/pharmacy/inventory/adjust', data),

  // Purchase Orders
  getPurchaseOrders: (page = 0, size = 20) => api.get('/pharmacy/purchase-orders', { params: { page, size } }),
  getPurchaseOrderById: (id) => api.get(`/pharmacy/purchase-orders/${id}`),
  createPurchaseOrder: (data) => api.post('/pharmacy/purchase-orders', data),
  updatePurchaseOrderStatus: (id, status) => api.patch(`/pharmacy/purchase-orders/${id}/status`, null, { params: { status } }),

  // Goods Receipts (GRN)
  getGoodsReceipts: (page = 0, size = 20) => api.get('/pharmacy/goods-receipts', { params: { page, size } }),
  getGoodsReceiptById: (id) => api.get(`/pharmacy/goods-receipts/${id}`),
  createGoodsReceipt: (data) => api.post('/pharmacy/goods-receipts', data),

  // Dispensing
  getDispensings: (page = 0, size = 20) => api.get('/pharmacy/dispensing', { params: { page, size } }),
  dispenseMedicine: (data) => api.post('/pharmacy/dispensing', data),
  getDispensingsByPrescription: (prescriptionId) => api.get(`/pharmacy/prescriptions/${prescriptionId}/dispensing`),

  // Returns
  getReturns: (page = 0, size = 20) => api.get('/pharmacy/returns', { params: { page, size } }),
  processPatientReturn: (data) => api.post('/pharmacy/returns/patient', data),
  processSupplierReturn: (data) => api.post('/pharmacy/returns/supplier', data),

  // Dashboards
  getPharmacyDashboard: () => api.get('/pharmacy/dashboard'),
  getInventoryDashboard: () => api.get('/pharmacy/inventory/dashboard')
};

export default pharmacyService;
