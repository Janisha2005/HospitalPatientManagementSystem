import api from './api';

const billingService = {
  // Charge Master
  getAllCharges: (params) => api.get('/billing/charges', { params }),
  getChargeById: (id) => api.get(`/billing/charges/${id}`),
  getChargesByCategory: (category) => api.get(`/billing/charges/category/${category}`),
  createCharge: (data) => api.post('/billing/charges', data),
  updateCharge: (id, data) => api.put(`/billing/charges/${id}`, data),
  updateChargeStatus: (id, active) => api.patch(`/billing/charges/${id}/status`, null, { params: { active } }),

  // Billing Accounts
  getAccountByPatientId: (patientId) => api.get(`/billing/accounts/patient/${patientId}`),

  // Bills
  getAllBills: (params) => api.get('/billing/bills', { params }),
  getBillById: (id) => api.get(`/billing/bills/${id}`),
  getBillByNumber: (billNumber) => api.get(`/billing/bills/code/${billNumber}`),
  getBillsByPatientId: (patientId, params) => api.get(`/billing/bills/patient/${patientId}`, { params }),
  createDraftBill: (data) => api.post('/billing/bills', data),
  addItemToBill: (billId, data) => api.post(`/billing/bills/${billId}/items`, data),
  finalizeBill: (billId) => api.post(`/billing/bills/${billId}/finalize`),
  cancelBill: (billId, reason) => api.post(`/billing/bills/${billId}/cancel`, null, { params: { reason } }),

  // Source Billing
  generateOpdBill: (opdVisitId) => api.post(`/billing/bills/opd/${opdVisitId}`),
  generatePharmacyBill: (dispensingId) => api.post(`/billing/bills/pharmacy/${dispensingId}`),
  generateLabBill: (labOrderId) => api.post(`/billing/bills/lab/${labOrderId}`),
  generateRadiologyBill: (radiologyOrderId) => api.post(`/billing/bills/radiology/${radiologyOrderId}`),
  generateIpdBill: (ipdAdmissionId) => api.post(`/billing/bills/ipd/${ipdAdmissionId}`),
  processDischargeClearance: (ipdAdmissionId, remarks) => api.post(`/billing/bills/ipd/${ipdAdmissionId}/clearance`, null, { params: { remarks } }),

  // Payments
  getAllPayments: (params) => api.get('/billing/payments', { params }),
  getPaymentById: (id) => api.get(`/billing/payments/${id}`),
  getPaymentsByBillId: (billId) => api.get(`/billing/payments/bill/${billId}`),
  getPaymentsByPatientId: (patientId, params) => api.get(`/billing/payments/patient/${patientId}`, { params }),
  recordPayment: (data) => api.post('/billing/payments', data),
  reversePayment: (id, reason) => api.post(`/billing/payments/${id}/reverse`, null, { params: { reason } }),

  // Refunds
  getAllRefunds: (params) => api.get('/billing/refunds', { params }),
  getRefundById: (id) => api.get(`/billing/refunds/${id}`),
  getRefundsByPatientId: (patientId, params) => api.get(`/billing/refunds/patient/${patientId}`, { params }),
  processRefund: (data) => api.post('/billing/refunds', data),

  // Credit Notes
  getAllCreditNotes: (params) => api.get('/billing/credit-notes', { params }),
  getCreditNoteById: (id) => api.get(`/billing/credit-notes/${id}`),
  getCreditNotesByPatientId: (patientId, params) => api.get(`/billing/credit-notes/patient/${patientId}`, { params }),
  createCreditNote: (data) => api.post('/billing/credit-notes', data),

  // Patient Ledger
  getPatientLedger: (patientId) => api.get(`/billing/patients/${patientId}/ledger`),

  // Dashboard & Reports
  getDashboardMetrics: () => api.get('/billing/dashboard'),
  getRevenueReport: (period) => api.get('/billing/reports/revenue', { params: { period } }),
};

export default billingService;
