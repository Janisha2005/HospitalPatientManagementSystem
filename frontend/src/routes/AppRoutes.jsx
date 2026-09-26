import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import MainLayout from '../layouts/MainLayout';

import LoginPage from '../pages/auth/LoginPage';
import DashboardPage from '../pages/admin/DashboardPage';
import UserManagementPage from '../pages/admin/UserManagementPage';
import DepartmentManagementPage from '../pages/admin/DepartmentManagementPage';

import PatientListPage from '../pages/patients/PatientListPage';
import PatientFormPage from '../pages/patients/PatientFormPage';
import PatientDetailPage from '../pages/patients/PatientDetailPage';

import DoctorListPage from '../pages/doctors/DoctorListPage';
import DoctorFormPage from '../pages/doctors/DoctorFormPage';
import DoctorDetailPage from '../pages/doctors/DoctorDetailPage';
import DoctorAvailabilityPage from '../pages/doctors/DoctorAvailabilityPage';

import AppointmentListPage from '../pages/appointments/AppointmentListPage';
import AppointmentFormPage from '../pages/appointments/AppointmentFormPage';
import AppointmentDetailPage from '../pages/appointments/AppointmentDetailPage';
import AppointmentCalendarPage from '../pages/appointments/AppointmentCalendarPage';

import OPDDashboardPage from '../pages/opd/OPDDashboardPage';
import OPDQueuePage from '../pages/opd/OPDQueuePage';
import OPDVisitPage from '../pages/opd/OPDVisitPage';
import ConsultationPage from '../pages/opd/ConsultationPage';

// Phase 4 Imports
import WardListPage from '../pages/wards/WardListPage';
import WardFormPage from '../pages/wards/WardFormPage';
import WardDetailPage from '../pages/wards/WardDetailPage';
import BedManagementPage from '../pages/beds/BedManagementPage';
import IPDDashboardPage from '../pages/ipd/IPDDashboardPage';
import AdmissionListPage from '../pages/ipd/AdmissionListPage';
import AdmissionFormPage from '../pages/ipd/AdmissionFormPage';
import AdmissionDetailPage from '../pages/ipd/AdmissionDetailPage';
import BedAllocationPage from '../pages/ipd/BedAllocationPage';
import WardTransferPage from '../pages/ipd/WardTransferPage';
import NursingStationPage from '../pages/ipd/NursingStationPage';
import InpatientVitalsPage from '../pages/ipd/InpatientVitalsPage';
import NursingNotesPage from '../pages/ipd/NursingNotesPage';
import DoctorProgressPage from '../pages/ipd/DoctorProgressPage';
import DischargePlanningPage from '../pages/ipd/DischargePlanningPage';
import DischargeSummaryPage from '../pages/ipd/DischargeSummaryPage';

// Phase 5 Imports
import PharmacyDashboardPage from '../pages/pharmacy/PharmacyDashboardPage';
import MedicineListPage from '../pages/pharmacy/MedicineListPage';
import MedicineFormPage from '../pages/pharmacy/MedicineFormPage';
import MedicineCategoryPage from '../pages/pharmacy/MedicineCategoryPage';
import SupplierListPage from '../pages/pharmacy/SupplierListPage';
import SupplierFormPage from '../pages/pharmacy/SupplierFormPage';
import InventoryDashboardPage from '../pages/pharmacy/InventoryDashboardPage';
import BatchListPage from '../pages/pharmacy/BatchListPage';
import StockAdjustmentPage from '../pages/pharmacy/StockAdjustmentPage';
import PurchaseOrderListPage from '../pages/pharmacy/PurchaseOrderListPage';
import GoodsReceiptListPage from '../pages/pharmacy/GoodsReceiptListPage';
import PrescriptionDispensingPage from '../pages/pharmacy/PrescriptionDispensingPage';
import PharmacyReturnPage from '../pages/pharmacy/PharmacyReturnPage';

// Phase 6 Imports
import BillingDashboardPage from '../pages/billing/BillingDashboardPage';
import ChargeMasterPage from '../pages/billing/ChargeMasterPage';
import BillListPage from '../pages/billing/BillListPage';
import BillFormPage from '../pages/billing/BillFormPage';
import BillDetailPage from '../pages/billing/BillDetailPage';
import BillInvoicePage from '../pages/billing/BillInvoicePage';
import PaymentListPage from '../pages/billing/PaymentListPage';
import RefundListPage from '../pages/billing/RefundListPage';
import CreditNoteListPage from '../pages/billing/CreditNoteListPage';
import PatientLedgerPage from '../pages/billing/PatientLedgerPage';
import BillingReportsPage from '../pages/billing/BillingReportsPage';

// Phase 7 Imports
import ExecutiveDashboardPage from '../pages/analytics/ExecutiveDashboardPage';

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      {/* Protected Routes inside MainLayout */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <MainLayout>
              <ExecutiveDashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/analytics/dashboard"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER', 'DOCTOR', 'NURSE', 'RECEPTIONIST']}>
            <MainLayout>
              <ExecutiveDashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Phase 5 Pharmacy & Inventory Routes */}
      <Route
        path="/pharmacy/dashboard"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST', 'DOCTOR']}>
            <MainLayout>
              <PharmacyDashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/medicines"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE', 'RECEPTIONIST']}>
            <MainLayout>
              <MedicineListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/medicines/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <MedicineFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/medicines/edit/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <MedicineFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/categories"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE']}>
            <MainLayout>
              <MedicineCategoryPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/suppliers"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <SupplierListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/suppliers/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <SupplierFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/suppliers/edit/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <SupplierFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/inventory/dashboard"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <InventoryDashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/inventory"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <MedicineListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/batches"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE']}>
            <MainLayout>
              <BatchListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/adjust-stock"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <StockAdjustmentPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/purchase-orders"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <PurchaseOrderListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/goods-receipts"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <GoodsReceiptListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/goods-receipts/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <GoodsReceiptListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/dispense"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <PrescriptionDispensingPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pharmacy/returns"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'PHARMACIST']}>
            <MainLayout>
              <PharmacyReturnPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Phase 6 Billing & Financial Routes */}
      <Route
        path="/billing/dashboard"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST']}>
            <MainLayout>
              <BillingDashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/charge-masters"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER']}>
            <MainLayout>
              <ChargeMasterPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/bills"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR', 'PATIENT']}>
            <MainLayout>
              <BillListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/bills/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST']}>
            <MainLayout>
              <BillFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/bills/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR', 'PATIENT']}>
            <MainLayout>
              <BillDetailPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/bills/:id/invoice"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR', 'PATIENT']}>
            <BillInvoicePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/payments"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT']}>
            <MainLayout>
              <PaymentListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/refunds"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER']}>
            <MainLayout>
              <RefundListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/credit-notes"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER']}>
            <MainLayout>
              <CreditNoteListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/ledger"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT']}>
            <MainLayout>
              <PatientLedgerPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/reports"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'BILLING_OFFICER']}>
            <MainLayout>
              <BillingReportsPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Wards & Beds */}
      <Route
        path="/wards"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST']}>
            <MainLayout>
              <WardListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/wards/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <MainLayout>
              <WardFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/wards/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST']}>
            <MainLayout>
              <WardDetailPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/wards/:id/edit"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <MainLayout>
              <WardFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/beds"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST']}>
            <MainLayout>
              <BedManagementPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* IPD Admissions & Inpatient Care */}
      <Route
        path="/ipd/dashboard"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST']}>
            <MainLayout>
              <IPDDashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT']}>
            <MainLayout>
              <AdmissionListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST']}>
            <MainLayout>
              <AdmissionFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT']}>
            <MainLayout>
              <AdmissionDetailPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/:id/allocate-bed"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST']}>
            <MainLayout>
              <BedAllocationPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/:id/transfer"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE']}>
            <MainLayout>
              <WardTransferPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/:id/vitals/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE']}>
            <MainLayout>
              <InpatientVitalsPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/:id/nursing-notes/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE']}>
            <MainLayout>
              <NursingNotesPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/:id/progress-notes/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR']}>
            <MainLayout>
              <DoctorProgressPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/:id/discharge-plan"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'NURSE']}>
            <MainLayout>
              <DischargePlanningPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ipd/admissions/:id/discharge-summary"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR']}>
            <MainLayout>
              <DischargeSummaryPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/ipd/nursing-station"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'NURSE', 'DOCTOR']}>
            <MainLayout>
              <NursingStationPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Appointments */}
      <Route
        path="/appointments"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT']}>
            <MainLayout>
              <AppointmentListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/appointments/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'RECEPTIONIST', 'PATIENT']}>
            <MainLayout>
              <AppointmentFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/appointments/calendar"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE']}>
            <MainLayout>
              <AppointmentCalendarPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/appointments/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT']}>
            <MainLayout>
              <AppointmentDetailPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* OPD */}
      <Route
        path="/opd/dashboard"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE']}>
            <MainLayout>
              <OPDDashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/opd/queue"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE']}>
            <MainLayout>
              <OPDQueuePage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/opd/visits/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT']}>
            <MainLayout>
              <OPDVisitPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/opd/consultation/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR']}>
            <MainLayout>
              <ConsultationPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Patients */}
      <Route
        path="/patients"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE']}>
            <MainLayout>
              <PatientListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/patients/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'RECEPTIONIST']}>
            <MainLayout>
              <PatientFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/patients/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT']}>
            <MainLayout>
              <PatientDetailPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/patients/:id/edit"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <MainLayout>
              <PatientFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Doctors */}
      <Route
        path="/doctors"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE']}>
            <MainLayout>
              <DoctorListPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/doctors/new"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <MainLayout>
              <DoctorFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/doctors/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE']}>
            <MainLayout>
              <DoctorDetailPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/doctors/:id/edit"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <MainLayout>
              <DoctorFormPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/doctors/:doctorId/availability"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'DOCTOR']}>
            <MainLayout>
              <DoctorAvailabilityPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Admin Modules */}
      <Route
        path="/admin/users"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <MainLayout>
              <UserManagementPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/departments"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <MainLayout>
              <DepartmentManagementPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};

export default AppRoutes;
