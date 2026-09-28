# PulseCare HMS — Hospital Management System

A full-stack Hospital Management System designed to manage core hospital operations across outpatient care, inpatient care, electronic health records, pharmacy, inventory, billing, and analytics.

The system is built with **Java, Spring Boot, React, and MySQL**, using REST APIs, JWT authentication, role-based access control, and a layered backend architecture.

---

## Overview

PulseCare HMS provides a centralized platform for managing hospital workflows from patient registration and appointment scheduling to clinical records, inpatient admission, pharmacy operations, billing, payments, and management analytics.

The application is organized into seven major development phases:

* Authentication and Administration
* OPD and Appointments
* EHR, Laboratory and Radiology
* IPD, Wards and Bed Management
* Pharmacy and Inventory
* Billing and Financial Management
* Reports and Analytics

The project contains both a **React frontend** and a **Spring Boot REST backend** connected to a **MySQL database**.

---

## Key Features

### Authentication & Administration

* JWT-based authentication
* BCrypt password hashing
* Role-Based Access Control (RBAC)
* User and role management
* Department management
* Audit logging

### Patient & Doctor Management

* Patient registration and profiles
* Doctor profiles
* Department directory
* Doctor availability management
* Patient medical information

### OPD & Appointments

* Appointment scheduling
* Appointment status management
* Doctor availability
* OPD registration
* Live OPD queue
* Consultation workflow
* Vitals and consultation notes

### Electronic Health Records

* Patient medical history
* Allergies and chronic conditions
* Diagnoses
* Treatment plans
* Prescriptions
* Prescription status management

### Laboratory & Radiology

* Laboratory investigation orders
* Sample collection workflow
* Result entry and verification
* Radiology order management
* Radiology reporting workflow

### IPD & Bed Management

* Ward management
* Bed management
* Patient admission
* Bed allocation
* Ward transfers
* Inpatient vitals
* Nursing notes
* Doctor progress notes
* Discharge planning
* Discharge summaries

### Pharmacy & Inventory

* Medicine master
* Supplier management
* Medicine batch management
* Purchase orders
* Goods receipt processing
* Inventory transactions
* FEFO-based medicine dispensing
* Stock adjustments
* Low-stock and expiry monitoring
* Patient medicine returns

### Billing & Financial Management

* Charge master
* Billing accounts
* OPD and IPD billing
* Pharmacy, laboratory and radiology billing
* Itemized invoices
* Payment processing
* Partial payments
* Refunds
* Credit notes
* Patient financial ledger
* IPD billing clearance
* Duplicate source-billing protection

### Reports & Analytics

* Executive dashboard
* Revenue and collection metrics
* Outstanding balances
* Refund and credit-note tracking
* Appointment analytics
* OPD/IPD statistics
* Bed occupancy
* Pharmacy statistics
* Laboratory and radiology statistics
* CSV report export

---

## Technology Stack

| Layer               | Technology                  |
| ------------------- | --------------------------- |
| Backend             | Java 21                     |
| Backend Framework   | Spring Boot 3.2.5           |
| Security            | Spring Security 6           |
| Authentication      | JWT / JJWT 0.12.5           |
| Password Security   | BCrypt                      |
| ORM                 | Spring Data JPA / Hibernate |
| Database            | MySQL 8.4                   |
| Testing Database    | H2                          |
| Frontend            | React 18                    |
| Frontend Build Tool | Vite                        |
| Routing             | React Router v6             |
| HTTP Client         | Axios                       |
| UI Framework        | Bootstrap 5                 |
| Icons               | Bootstrap Icons             |
| Backend Build Tool  | Maven                       |
| API Architecture    | REST                        |

---

## Architecture

PulseCare HMS follows a layered full-stack architecture.

```text
┌──────────────────────────────────────┐
│          React 18 Frontend           │
│        Vite + Bootstrap + Axios      │
└──────────────────┬───────────────────┘
                   │
                   │ REST API
                   │ Bearer JWT
                   ▼
┌──────────────────────────────────────┐
│       Spring Security 6              │
│       Authentication + RBAC          │
└──────────────────┬───────────────────┘
                   ▼
┌──────────────────────────────────────┐
│        REST Controller Layer         │
└──────────────────┬───────────────────┘
                   ▼
┌──────────────────────────────────────┐
│          Service Layer               │
│ Business Rules + Validation          │
│ Transaction Management               │
└──────────────────┬───────────────────┘
                   ▼
┌──────────────────────────────────────┐
│       Spring Data JPA Layer          │
│        Repositories + Queries        │
└──────────────────┬───────────────────┘
                   ▼
┌──────────────────────────────────────┐
│            MySQL 8.4                │
│          hospital_db                 │
└──────────────────────────────────────┘
```

### Authentication Flow

```text
User Login
    ↓
React Login Form
    ↓
POST /api/auth/login
    ↓
Spring Security
    ↓
Credential Verification
    ↓
JWT Generation
    ↓
React Stores Authentication State
    ↓
Axios Sends Bearer Token
    ↓
JWT Filter Validates Request
    ↓
RBAC Authorization
    ↓
Controller → Service → Repository
```

---

## Main Workflows

### OPD Workflow

```text
Patient Registration
        ↓
Appointment Booking
        ↓
Patient Check-in
        ↓
OPD Queue
        ↓
Consultation
        ↓
Vitals / EHR / Prescription
        ↓
Lab or Radiology Orders
        ↓
Billing
        ↓
Payment
```

### IPD Workflow

```text
Patient Admission
        ↓
Bed Allocation
        ↓
Inpatient Care
        ↓
Nursing & Vitals
        ↓
Doctor Progress Notes
        ↓
Medicines / Diagnostics
        ↓
Final Billing
        ↓
Payment Clearance
        ↓
Discharge Summary
        ↓
Bed Released
```

### Pharmacy Workflow

```text
Medicine Master
      ↓
Supplier
      ↓
Purchase Order
      ↓
Goods Receipt
      ↓
Batch & Inventory
      ↓
Prescription
      ↓
FEFO Dispensing
      ↓
Inventory Transaction
      ↓
Pharmacy Billing
```

### Billing Workflow

```text
Charge Master
      ↓
Billing Account
      ↓
Bill Creation
      ↓
Invoice Finalization
      ↓
Payment
      ↓
Patient Ledger
      ↓
Refund / Credit Note
```

---

## Project Structure

```text
HospitalPatientManagementSystem/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/hospital/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── entity/
│   │   │   │       ├── exception/
│   │   │   │       ├── repository/
│   │   │   │       ├── security/
│   │   │   │       └── service/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── context/
│   │   └── App.jsx
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

---

## Roles

The system supports seven application roles:

* **ADMIN** — System administration and overall management
* **DOCTOR** — Clinical and consultation operations
* **NURSE** — Nursing and inpatient care
* **RECEPTIONIST** — Patient registration, appointments and operational tasks
* **PATIENT** — Patient-specific information and services
* **PHARMACIST** — Pharmacy and inventory operations
* **BILLING_OFFICER** — Billing and financial operations

Access to backend APIs is controlled using Spring Security and role-based authorization.

---

## Local Setup

### Prerequisites

Install:

* Java 21+
* Node.js 18+
* npm
* Maven 3.9+
* MySQL 8+

### 1. Create the Database

Start MySQL and create the application database:

```sql
CREATE DATABASE IF NOT EXISTS hospital_db;
```

Configure the database credentials in the backend application configuration.

### 2. Start the Backend

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

Backend:

```text
http://localhost:8082
```

### 3. Start the Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

## Testing

### Backend Tests

```bash
cd backend
mvn clean test
```

The current verified test suite contains:

```text
47 tests
0 failures
0 errors
0 skipped
BUILD SUCCESS
```

The tests use an H2 in-memory database for automated testing.

### Frontend Production Build

```bash
cd frontend
npm run build
```

The production frontend is bundled using Vite.

---

## Demo Account

The project contains fictional demonstration data for local development and demonstration purposes.

| Role            | Username             | Password           | Name               |
| --------------- | -------------------- | ------------------ | ------------------ |
| ADMIN           | `admin`              | `Admin@123`        | Janisha S          |
| DOCTOR          | `doc_smith`          | `Doctor@123`       | Dr. Arjun Krishnan |
| DOCTOR          | `doc_davis`          | `Doctor@123`       | Dr. Sunita Rao     |
| NURSE           | `nurse_joy`          | `Nurse@123`        | Anitha Raman       |
| RECEPTIONIST    | `receptionist_clara` | `Receptionist@123` | Kavitha Menon      |
| PHARMACIST      | `pharmacist_rahul`   | `Pharmacist@123`   | Rahul Verma        |
| BILLING OFFICER | `billing_vikram`     | `Billing@123`      | Vikram Malhotra    |
| PATIENT         | `patient_john`       | `Patient@123`      | Arun Kumar         |

> These credentials are intended for local/demo use. Change or remove demo credentials before deploying the application publicly.

---

## Demo Data

The application includes synthetic demonstration data covering the major modules, including:

* Users and roles
* Departments
* Doctors
* Patients
* Appointments
* OPD visits
* EHR records
* Prescriptions
* Medicines
* Suppliers
* Inventory batches
* Purchase orders
* Goods receipts
* Laboratory orders
* Radiology orders
* Wards and beds
* IPD admissions
* Nursing records
* Discharge records
* Charge masters
* Billing accounts
* Bills
* Payments
* Refunds
* Credit notes
* Financial ledger entries

All demonstration records are fictional.

---

## Security

The application implements:

* JWT-based stateless authentication
* BCrypt password hashing
* Spring Security
* Role-based API authorization
* Bearer token authentication
* Backend-side authorization
* Request validation
* Transactional business operations
* Database-level relationships and constraints

Frontend route protection is supplemented by backend authorization, so API access is not dependent solely on frontend UI restrictions.

---

## Project Status

The seven planned development phases have been implemented and verified.

Current verification includes:

* 47 backend automated tests passed
* 0 test failures
* 0 test errors
* 0 skipped tests
* Successful frontend production build
* React frontend connected to Spring Boot REST APIs
* MySQL database integration verified
* JWT authentication verified
* Role-based authorization implemented
* Major clinical, pharmacy, billing and analytics workflows verified

**Status: Ready for internship demonstration and project presentation.**

---

## Future Enhancements

Potential extensions include:

* Insurance and TPA claim management
* Operation Theatre management
* Blood bank management
* Ambulance and emergency management
* Telemedicine
* Advanced notification system
* Deployment with cloud infrastructure
* Automated backups and monitoring

---

## Author

**Janisha S.**

BCA Graduate | Master's in Applied Data Science

GitHub: [github.com/Janisha2005](https://github.com/Janisha2005)
