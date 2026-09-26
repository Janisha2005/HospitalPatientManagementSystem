# Hospital Management System (HMS) — Full-Stack India-Localized Platform

An enterprise-grade, full-stack **Hospital Management System (HMS)** built with **Java 21**, **Spring Boot 3.2.5**, **React 18**, **Bootstrap 5**, and **MySQL 8.4**, engineered specifically for Indian hospital workflows, compliance, currency standards (`₹` / `INR`), and timezones (`Asia/Kolkata`).

---

## 1. Executive Summary & Project Overview

This Hospital Management System spans 7 full development phases, covering end-to-end clinical, operational, inpatient, outpatient, inventory, financial, and analytical hospital functions.

### Core Localization Standards (`en-IN`)
* **Currency**: Indian Rupee (`INR` / `₹`) with `BigDecimal` precision.
* **Time Zone**: Indian Standard Time (`Asia/Kolkata` / `UTC+05:30`).
* **Phone Standard**: Indian Mobile Format (`+91XXXXXXXXXX` / 10 digits).
* **PIN Standard**: 6-digit Indian PIN Code (`^[1-9][0-9]{5}$`).
* **Taxation & Licensing**: Support for Indian GSTIN formats, Drug License numbers, and MCI registration formats.

---

## 2. Technology Stack

| Layer | Technologies & Frameworks |
|---|---|
| **Backend Framework** | Java 21, Spring Boot 3.2.5, Spring Data JPA, Hibernate ORM |
| **Security & Auth** | Spring Security 6, JJWT 0.12.5 (Stateless JWT), BCrypt Password Hashing |
| **Database** | MySQL 8.4 Server (production/dev), H2 In-Memory Database (automated testing) |
| **Build Tooling** | Apache Maven (Backend), Vite (Frontend) |
| **Frontend Framework** | React 18 (ESNext), React Router v6, Axios |
| **UI & Styling** | Bootstrap 5, Bootstrap Icons, Vanilla CSS |

---

## 3. Architecture & Data Flow

```text
React 18 Frontend (Vite Single Page Application)
       │
       ▼  (Axios REST Requests with Bearer JWT Header)
Spring Security 6 & Controller Layer (REST Controllers)
       │
       ▼  (Service Layer Business Rules & RBAC Annotations)
Service Layer (Transactional Operations & Validations)
       │
       ▼  (Spring Data JPA Repositories)
Data Access Layer (Entities & Queries)
       │
       ▼  (JDBC Driver)
MySQL 8.4 Database / H2 Test Engine
```

---

## 4. System Modules Across Phases 1–7

### Phase 1 — Core Foundation, Auth & Administration
* **Authentication**: JWT Token generation, validation, refresh, and login/logout audit logging.
* **RBAC**: 7 System Roles (`ADMIN`, `DOCTOR`, `NURSE`, `RECEPTIONIST`, `PATIENT`, `PHARMACIST`, `BILLING_OFFICER`).
* **Master Data**: User management, Department directory (`CARDIO`, `ORTHO`, `GENERAL`, `PEDIATRICS`, `DERMATOLOGY`), Patient registration (`PAT-YYYY-XXXXXX`), and Doctor profiles (`DOC-YYYY-XXXXXX`).

### Phase 2 — Outpatient Care (OPD) & Appointments
* **Doctor Availability**: Day-of-week slot duration and time window management.
* **Appointments**: Booking (`NEW_CONSULTATION`, `FOLLOW_UP`, `EMERGENCY`), status transitions (`SCHEDULED`, `CHECKED_IN`, `COMPLETED`, `CANCELLED`, `NO_SHOW`), and interactive calendar view.
* **OPD Live Queue**: Room-wise queue tracking, triage vitals recording, consultation notes, and visit completion.

### Phase 3 — Electronic Health Records (EHR) & Ancillary Services
* **Clinical History**: Allergic reactions, chronic conditions tracking, diagnosis recording (ICD-10 standard formatting), and treatment plans.
* **Prescriptions**: Multi-item prescription writing with dosage, frequency, duration, and instructions.
* **Laboratory & Radiology Orders**: Lab investigation orders (CBC, FBS, HbA1c, LFT, KFT) and Radiology modality studies (X-Ray, Ultrasound, CT, MRI, ECG) with result entry workflows.

### Phase 4 — Inpatient Care (IPD), Wards & Beds
* **Ward & Bed Infrastructure**: Ward capacity management, Bed mapping (`AVAILABLE`, `OCCUPIED`, `CLEANING`, `MAINTENANCE`), and live occupancy matrix.
* **IPD Admissions**: Inpatient admission (`ADM-YYYY-XXXXXX`), bed allocation, ward transfers, daily vitals logs, nursing care notes, doctor progress notes, discharge planning, and discharge summary generation.

### Phase 5 — Pharmacy & Inventory Management
* **Inventory Master**: Medicine catalog (`MED-YYYY-XXXXXX`), dosage forms, therapeutic categories, and supplier directory (`SUP-YYYY-XXXXXX`).
* **Procurement & Stock Control**: Purchase Orders (`PO-YYYY-XXXXXX`), Goods Receipts (GRN: `GRN-YYYY-XXXXXX`), Batch tracking (`BAT-YYYY-XXXXXX`) with MRP/Cost/Expiry dates, FEFO dispensing (`DSP-YYYY-XXXXXX`), stock adjustments, low-stock, and near-expiry alerts.

### Phase 6 — Billing, Invoicing & Financial Management
* **Charge Master**: Tariff catalog (`CONSULTATION`, `PROCEDURE`, `LABORATORY`, `RADIOLOGY`, `BED`, `PHARMACY`, `EMERGENCY`, `REGISTRATION`) with base rates and tax rates.
* **Billing Accounts & Invoices**: Itemized draft and finalized bills (`BILL-YYYY-XXXXXX`), source billing from OPD/IPD/Pharmacy/Lab/Radiology, duplicate charge prevention, partial payments (`PAY-YYYY-XXXXXX`), refunds (`REF-YYYY-XXXXXX`), credit notes (`CN-YYYY-XXXXXX`), and patient double-entry financial ledgers (`LEG-YYYY-XXXXXX`).

### Phase 7 — Reports, Analytics & Executive Dashboards
* **Executive Dashboard**: Hospital-wide financial highlights (Total Billed, Total Collected, Total Outstanding, Net Revenue), Clinical & OPD/IPD metrics, Bed occupancy rates, Appointment completion breakdown, and Pharmacy/Lab/Radiology utilization statistics.
* **Tabular Operational Reports**: Payment Collection, Outstanding Balances with aging buckets, Bed Occupancy by Ward, OPD/IPD visits, Pharmacy stock reports, and direct CSV file export (`/api/reports/export/csv`).

---

## 5. End-to-End Clinical & Operational Workflows

### OPD Workflow
```text
Patient Registration → Appointment Booking → Patient Check-in 
→ OPD Live Queue → Consultation & Vitals → EHR & Prescriptions / Lab Orders 
→ OPD Source Billing → Payment Processing → Patient Ledger Updated
```

### IPD Workflow
```text
Patient Admission → Bed Allocation → Daily Nursing & Vitals → Doctor Progress Notes 
→ Inpatient Medicines & Diagnostics → Final IPD Billing → Payment Clearance 
→ Discharge Clearance → Discharge Summary → Bed Returned to Cleaning State
```

---

## 6. Installation & Execution Guide

### Prerequisites
* **Java**: JDK 21+
* **Node.js**: v18+ & `npm`
* **Build Tools**: Apache Maven 3.9+
* **Database**: MySQL Server 8.0+

### Database Initialization
```sql
CREATE DATABASE IF NOT EXISTS hospital_db;
```

### Backend Setup
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
* Backend server starts at: `http://localhost:8082`

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
* Frontend dev server starts at: `http://localhost:5173`

---

## 7. Verification & Testing Commands

### Backend Automated Test Suite
```bash
cd backend
mvn clean test
```
* Executes 47 Spring Boot integration & controller tests using H2 in-memory test configuration (`application-test.properties`).

### Frontend Production Build
```bash
cd frontend
npm run build
```
* Compiles and bundles production static assets using Vite.

---

## 8. Demo Credentials (Fictional Indian Demo Data)

| Role | Username | Password | Full Name |
|---|---|---|---|
| **ADMIN** | `admin` | `Admin@123` | Rajesh Sharma |
| **DOCTOR** | `doc_smith` | `Doctor@123` | Dr. Arjun Krishnan |
| **DOCTOR** | `doc_davis` | `Doctor@123` | Dr. Sunita Rao |
| **NURSE** | `nurse_joy` | `Nurse@123` | Anitha Raman |
| **RECEPTIONIST** | `receptionist_clara` | `Receptionist@123` | Kavitha Menon |
| **PHARMACIST** | `pharmacist_rahul` | `Pharmacist@123` | Rahul Verma |
| **BILLING OFFICER** | `billing_vikram` | `Billing@123` | Vikram Malhotra |
| **PATIENT** | `patient_john` | `Patient@123` | Arun Kumar |

---

## 9. Viva Questions & Key Concepts

### Java & Spring Boot
1. **Why Java 21 & Spring Boot 3.2.5?**: Virtual threads support, record classes, pattern matching, baseline Spring Security 6 compatibility, and native Java 21 LTS runtime.
2. **What is `@Transactional`?**: Ensures ACID compliance across database operations (e.g., creating a payment simultaneously updates the bill status and writes a patient ledger entry).
3. **What is Spring Data JPA?**: Provides ORM abstraction mapping Java entity classes to MySQL tables without raw SQL strings.

### Security & RBAC
4. **How does JWT Authentication work?**: The backend verifies credentials upon login, generates a signed HS512 JWT token containing role claims, which the React client sends in the `Authorization: Bearer <token>` header for stateless validation.
5. **Why server-side RBAC over frontend route guards?**: Frontend route guards only control UI navigation visibility; server-side `@PreAuthorize("hasRole(...)")` annotations strictly enforce authority on every API request.

### Financial & Inventory
6. **Why `DECIMAL(12,2)` for financial values?**: Prevents floating-point precision loss inherent in `float` or `double` types when processing currency totals.
7. **What is FEFO in Pharmacy?**: First-Expired-First-Out dispensing logic ensures batches with the earliest expiry dates are issued first to reduce waste.

---

## 10. Future Enhancements

The following modules represent potential future extensions outside the current internship project scope:
1. **Insurance & TPA Claims Engine**: Automated pre-authorization, claim filing, and third-party settlement tracking.
2. **Operation Theatre (OT) & Surgical Workflow**: Surgery scheduling, OT roster, anesthesia notes, and surgical checklists.
3. **Blood Bank Management**: Donor registration, blood component separation, cross-matching, and inventory management.
4. **Ambulance & Emergency Response**: Vehicle tracking, driver dispatch, and casualty triage integration.
5. **Telemedicine & Video Consultation**: WebRTC video calls, digital prescriptions, and remote patient monitoring.
