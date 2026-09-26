import React from 'react';
import { NavLink } from 'react-router-dom';
import { authService } from '../services/authService';

const Sidebar = ({ isOpen }) => {
  const isAdmin = authService.hasRole('ADMIN');
  const isDoctor = authService.hasRole('DOCTOR');
  const isNurse = authService.hasRole('NURSE');
  const isReceptionist = authService.hasRole('RECEPTIONIST');
  const isPharmacist = authService.hasRole('PHARMACIST');
  const isBillingOfficer = authService.hasRole('BILLING_OFFICER');

  return (
    <div className={`sidebar bg-white border-end shadow-sm ${isOpen ? 'show' : ''}`}>
      <div className="py-3 px-3">
        <div className="text-uppercase text-muted fw-bold mb-2 px-3" style={{ fontSize: '0.7rem', letterSpacing: '0.5px' }}>
          Main Navigation
        </div>
        <ul className="nav nav-pills flex-column mb-auto">
          <li className="nav-item mb-1">
            <NavLink
              to="/dashboard"
              className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
            >
              <i className="bi bi-speedometer2 me-2"></i>
              <span>Dashboard</span>
            </NavLink>
          </li>
          <li className="nav-item mb-1">
            <NavLink
              to="/analytics/dashboard"
              className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
            >
              <i className="bi bi-pie-chart-fill me-2 text-primary"></i>
              <span>Executive Analytics</span>
            </NavLink>
          </li>

          {/* Phase 5 Pharmacy & Inventory Management Section */}
          {(isAdmin || isPharmacist || isDoctor) && (
            <>
              <div className="text-uppercase text-muted fw-bold mt-2 mb-2 px-3" style={{ fontSize: '0.7rem', letterSpacing: '0.5px' }}>
                Pharmacy & Inventory
              </div>
              <li className="nav-item mb-1">
                <NavLink
                  to="/pharmacy/dashboard"
                  className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                >
                  <i className="bi bi-shop me-2"></i>
                  <span>Pharmacy Dashboard</span>
                </NavLink>
              </li>
              <li className="nav-item mb-1">
                <NavLink
                  to="/pharmacy/medicines"
                  className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                >
                  <i className="bi bi-prescription2 me-2"></i>
                  <span>Medicine Catalog</span>
                </NavLink>
              </li>
              {(isAdmin || isPharmacist) && (
                <>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/pharmacy/dispense"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-capsule me-2"></i>
                      <span>Dispense Queue</span>
                    </NavLink>
                  </li>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/pharmacy/inventory/dashboard"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-boxes me-2"></i>
                      <span>Inventory & Batches</span>
                    </NavLink>
                  </li>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/pharmacy/purchase-orders"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-cart-check me-2"></i>
                      <span>Purchase Orders</span>
                    </NavLink>
                  </li>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/pharmacy/goods-receipts"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-box-seam me-2"></i>
                      <span>Goods Receipts (GRN)</span>
                    </NavLink>
                  </li>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/pharmacy/suppliers"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-truck me-2"></i>
                      <span>Suppliers</span>
                    </NavLink>
                  </li>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/pharmacy/returns"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-arrow-counterclockwise me-2"></i>
                      <span>Pharmacy Returns</span>
                    </NavLink>
                  </li>
                </>
              )}
            </>
          )}

          {/* Phase 6 Billing & Financial Management Section */}
          {(isAdmin || isBillingOfficer || isReceptionist) && (
            <>
              <div className="text-uppercase text-muted fw-bold mt-2 mb-2 px-3" style={{ fontSize: '0.7rem', letterSpacing: '0.5px' }}>
                Billing & Financials
              </div>
              <li className="nav-item mb-1">
                <NavLink
                  to="/billing/dashboard"
                  className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                >
                  <i className="bi bi-wallet2 me-2"></i>
                  <span>Billing Dashboard</span>
                </NavLink>
              </li>
              <li className="nav-item mb-1">
                <NavLink
                  to="/billing/bills"
                  className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                >
                  <i className="bi bi-receipt me-2"></i>
                  <span>Bills & Invoices</span>
                </NavLink>
              </li>
              <li className="nav-item mb-1">
                <NavLink
                  to="/billing/payments"
                  className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                >
                  <i className="bi bi-cash-stack me-2"></i>
                  <span>Payments</span>
                </NavLink>
              </li>
              {(isAdmin || isBillingOfficer) && (
                <>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/billing/charge-masters"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-tags me-2"></i>
                      <span>Charge Master</span>
                    </NavLink>
                  </li>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/billing/refunds"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-arrow-left-right me-2"></i>
                      <span>Refunds</span>
                    </NavLink>
                  </li>
                  <li className="nav-item mb-1">
                    <NavLink
                      to="/billing/credit-notes"
                      className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                    >
                      <i className="bi bi-card-heading me-2"></i>
                      <span>Credit Notes</span>
                    </NavLink>
                  </li>
                </>
              )}
              <li className="nav-item mb-1">
                <NavLink
                  to="/billing/ledger"
                  className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                >
                  <i className="bi bi-journal-text me-2"></i>
                  <span>Patient Ledger</span>
                </NavLink>
              </li>
              {(isAdmin || isBillingOfficer) && (
                <li className="nav-item mb-1">
                  <NavLink
                    to="/billing/reports"
                    className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                  >
                    <i className="bi bi-bar-chart-line me-2"></i>
                    <span>Financial Reports</span>
                  </NavLink>
                </li>
              )}
            </>
          )}

          {/* Phase 4 IPD & Inpatient Care Section */}
          <div className="text-uppercase text-muted fw-bold mt-2 mb-2 px-3" style={{ fontSize: '0.7rem', letterSpacing: '0.5px' }}>
            Inpatient Care (IPD)
          </div>

          {(isAdmin || isDoctor || isNurse || isReceptionist) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/ipd/dashboard"
                className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
              >
                <i className="bi bi-hospital me-2"></i>
                <span>IPD Dashboard</span>
              </NavLink>
            </li>
          )}

          <li className="nav-item mb-1">
            <NavLink
              to="/ipd/admissions"
              className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
            >
              <i className="bi bi-clipboard2-pulse me-2"></i>
              <span>IPD Admissions</span>
            </NavLink>
          </li>

          {(isAdmin || isNurse || isDoctor) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/ipd/nursing-station"
                className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
              >
                <i className="bi bi-person-badge me-2"></i>
                <span>Nursing Station</span>
              </NavLink>
            </li>
          )}

          {(isAdmin || isDoctor || isNurse || isReceptionist) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/wards"
                className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
              >
                <i className="bi bi-building me-2"></i>
                <span>Ward Management</span>
              </NavLink>
            </li>
          )}

          {(isAdmin || isDoctor || isNurse || isReceptionist) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/beds"
                className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
              >
                <i className="bi bi-lamp me-2"></i>
                <span>Bed Map & Occupancy</span>
              </NavLink>
            </li>
          )}

          <div className="text-uppercase text-muted fw-bold mt-2 mb-2 px-3" style={{ fontSize: '0.7rem', letterSpacing: '0.5px' }}>
            Outpatient & Ancillary
          </div>

          {/* Appointments Link */}
          <li className="nav-item mb-1">
            <NavLink
              to="/appointments"
              className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
            >
              <i className="bi bi-calendar-check me-2"></i>
              <span>Appointments</span>
            </NavLink>
          </li>

          {/* OPD Queue Link */}
          {(isAdmin || isDoctor || isNurse || isReceptionist) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/opd/queue"
                className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
              >
                <i className="bi bi-person-lines-fill me-2"></i>
                <span>OPD Live Queue</span>
              </NavLink>
            </li>
          )}

          {/* OPD Dashboard */}
          {(isAdmin || isDoctor || isNurse || isReceptionist) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/opd/dashboard"
                className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
              >
                <i className="bi bi-graph-up me-2"></i>
                <span>OPD Dashboard</span>
              </NavLink>
            </li>
          )}

          {(isAdmin || isDoctor || isNurse || isReceptionist) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/patients"
                className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
              >
                <i className="bi bi-people me-2"></i>
                <span>Patients</span>
              </NavLink>
            </li>
          )}

          {/* Prescriptions */}
          <li className="nav-item mb-1">
            <NavLink
              to="/prescriptions"
              className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
            >
              <i className="bi bi-capsule me-2"></i>
              <span>Prescriptions</span>
            </NavLink>
          </li>

          {/* Laboratory */}
          <li className="nav-item mb-1">
            <NavLink
              to="/lab/orders"
              className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
            >
              <i className="bi bi-journal-medical me-2"></i>
              <span>Laboratory</span>
            </NavLink>
          </li>

          {/* Radiology */}
          <li className="nav-item mb-1">
            <NavLink
              to="/radiology/orders"
              className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
            >
              <i className="bi bi-disc me-2"></i>
              <span>Radiology</span>
            </NavLink>
          </li>

          {(isAdmin || isDoctor || isNurse || isReceptionist) && (
            <li className="nav-item mb-1">
              <NavLink
                to="/doctors"
                className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
              >
                <i className="bi bi-person-badge me-2"></i>
                <span>Doctors</span>
              </NavLink>
            </li>
          )}

          {isAdmin && (
            <>
              <div className="text-uppercase text-muted fw-bold mt-3 mb-2 px-3" style={{ fontSize: '0.7rem', letterSpacing: '0.5px' }}>
                Administration
              </div>
              <li className="nav-item mb-1">
                <NavLink
                  to="/admin/departments"
                  className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                >
                  <i className="bi bi-building me-2"></i>
                  <span>Departments</span>
                </NavLink>
              </li>
              <li className="nav-item mb-1">
                <NavLink
                  to="/admin/users"
                  className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? 'active' : 'text-dark'}`}
                >
                  <i className="bi bi-shield-lock me-2"></i>
                  <span>User Management</span>
                </NavLink>
              </li>
            </>
          )}
        </ul>
      </div>
    </div>
  );
};

export default Sidebar;
