import api from './api';

export const treatmentPlanService = {
  getPatientTreatmentPlans: async (patientId) => {
    const response = await api.get(`/patients/${patientId}/treatment-plans`);
    return response.data;
  },

  getVisitTreatmentPlans: async (opdVisitId) => {
    const response = await api.get(`/opd/visits/${opdVisitId}/treatment-plans`);
    return response.data;
  },

  createTreatmentPlan: async (patientId, planData) => {
    const response = await api.post(`/patients/${patientId}/treatment-plans`, planData);
    return response.data;
  },

  supersedeTreatmentPlan: async (planId, planData) => {
    const response = await api.put(`/treatment-plans/${planId}/supersede`, planData);
    return response.data;
  },
};

export default treatmentPlanService;
