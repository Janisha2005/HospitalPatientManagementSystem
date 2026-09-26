package com.hospital.service;

import com.hospital.dto.BillDto;
import com.hospital.dto.BillItemDto;
import com.hospital.dto.DischargeClearanceDto;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.DuplicateSourceBillingException;
import com.hospital.exception.InvalidBillingStateException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PatientRepository patientRepository;
    private final BillingAccountService billingAccountService;
    private final OpdVisitRepository opdVisitRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final PharmacyDispensingRepository dispensingRepository;
    private final LabOrderRepository labOrderRepository;
    private final RadiologyOrderRepository radiologyOrderRepository;
    private final ChargeMasterRepository chargeMasterRepository;
    private final PatientLedgerService ledgerService;
    private final AuditLogService auditLogService;

    public static final BigDecimal MAX_NON_ADMIN_DISCOUNT_PERCENT = new BigDecimal("20.00");

    @Transactional(readOnly = true)
    public Page<BillDto> getAllBills(Pageable pageable) {
        return billRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<BillDto> searchBills(String query, Pageable pageable) {
        return billRepository.searchBills(query, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public BillDto getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + id));
        return mapToDto(bill);
    }

    @Transactional(readOnly = true)
    public BillDto getBillByNumber(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with number: " + billNumber));
        return mapToDto(bill);
    }

    @Transactional(readOnly = true)
    public Page<BillDto> getBillsByPatientId(Long patientId, Pageable pageable) {
        return billRepository.findByPatientId(patientId, pageable).map(this::mapToDto);
    }

    @Transactional
    public BillDto createDraftBill(BillDto dto, String username) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + dto.getPatientId()));

        BillingAccount account = billingAccountService.getOrCreateAccountForPatient(patient.getId());

        OpdVisit opdVisit = null;
        if (dto.getOpdVisitId() != null) {
            opdVisit = opdVisitRepository.findById(dto.getOpdVisitId()).orElse(null);
        }

        IpdAdmission ipdAdmission = null;
        if (dto.getIpdAdmissionId() != null) {
            ipdAdmission = ipdAdmissionRepository.findById(dto.getIpdAdmissionId()).orElse(null);
        }

        String billNumber = generateBillNumber();

        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .patient(patient)
                .billingAccount(account)
                .opdVisit(opdVisit)
                .ipdAdmission(ipdAdmission)
                .billType(dto.getBillType())
                .billDate(dto.getBillDate() != null ? dto.getBillDate() : LocalDate.now())
                .dueDate(dto.getDueDate() != null ? dto.getDueDate() : LocalDate.now().plusDays(15))
                .status(BillStatus.DRAFT)
                .createdBy(username)
                .notes(dto.getNotes())
                .items(new ArrayList<>())
                .build();

        Bill savedBill = billRepository.save(bill);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (BillItemDto itemDto : dto.getItems()) {
                addBillItemToBill(savedBill, itemDto);
            }
        }

        calculateBillTotals(savedBill);
        Bill finalBill = billRepository.save(savedBill);

        auditLogService.logAction("CREATE_DRAFT_BILL", "Bill", finalBill.getId().toString(),
                "Created draft bill: " + finalBill.getBillNumber() + " for Patient: " + patient.getPatientId());

        return mapToDto(finalBill);
    }

    @Transactional
    public BillDto addItemToBill(Long billId, BillItemDto itemDto, String username) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        if (bill.getStatus() != BillStatus.DRAFT) {
            throw new InvalidBillingStateException("Cannot add items to bill with status: " + bill.getStatus() + ". Only DRAFT bills can be edited.");
        }

        addBillItemToBill(bill, itemDto);
        calculateBillTotals(bill);
        Bill saved = billRepository.save(bill);

        auditLogService.logAction("ADD_BILL_ITEM", "Bill", saved.getId().toString(),
                "Added item " + itemDto.getDescription() + " to bill: " + saved.getBillNumber());

        return mapToDto(saved);
    }

    @Transactional
    public BillDto finalizeBill(Long billId, String username, boolean isAdmin) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        if (bill.getStatus() != BillStatus.DRAFT) {
            throw new InvalidBillingStateException("Only DRAFT bills can be finalized. Current status: " + bill.getStatus());
        }

        if (bill.getItems() == null || bill.getItems().isEmpty()) {
            throw new BadRequestException("Cannot finalize an empty bill with no line items.");
        }

        // Validate discount cap for non-admins
        if (!isAdmin && bill.getDiscountAmount() != null && bill.getSubtotal() != null && bill.getSubtotal().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discPct = bill.getDiscountAmount().multiply(new BigDecimal("100")).divide(bill.getSubtotal(), 2, RoundingMode.HALF_UP);
            if (discPct.compareTo(MAX_NON_ADMIN_DISCOUNT_PERCENT) > 0) {
                throw new BadRequestException("Discount percentage (" + discPct + "%) exceeds maximum allowed limit of " + MAX_NON_ADMIN_DISCOUNT_PERCENT + "% for non-admin users.");
            }
        }

        calculateBillTotals(bill);
        bill.setStatus(BillStatus.GENERATED);
        Bill saved = billRepository.save(bill);

        // Record patient debit ledger entry
        ledgerService.recordEntry(bill.getPatient(), saved, null, null, null,
                LedgerEntryType.BILL, saved.getGrandTotal(), BigDecimal.ZERO,
                "Invoice generated: " + saved.getBillNumber() + " (" + saved.getBillType() + ")");

        auditLogService.logAction("FINALIZE_BILL", "Bill", saved.getId().toString(),
                "Finalized bill: " + saved.getBillNumber() + " Total: ₹" + saved.getGrandTotal());

        return mapToDto(saved);
    }

    @Transactional
    public BillDto cancelBill(Long billId, String reason, String username) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        if (bill.getStatus() == BillStatus.PAID || bill.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidBillingStateException("Cannot cancel a bill that has payments recorded. Use Reversal or Credit Note.");
        }

        bill.setStatus(BillStatus.CANCELLED);
        bill.setNotes((bill.getNotes() != null ? bill.getNotes() + "\n" : "") + "Cancelled by " + username + ". Reason: " + reason);
        Bill saved = billRepository.save(bill);

        auditLogService.logAction("CANCEL_BILL", "Bill", saved.getId().toString(),
                "Cancelled bill: " + saved.getBillNumber());

        return mapToDto(saved);
    }

    // Source Billing Helpers

    @Transactional
    public BillDto generateOpdBill(Long opdVisitId, String username) {
        OpdVisit visit = opdVisitRepository.findById(opdVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with ID: " + opdVisitId));

        if (billItemRepository.existsBySourceTypeAndSourceId(BillSourceType.CONSULTATION, opdVisitId)) {
            throw new DuplicateSourceBillingException("OPD Visit ID " + opdVisitId + " has already been billed.");
        }

        Patient patient = visit.getPatient();
        BillingAccount account = billingAccountService.getOrCreateAccountForPatient(patient.getId());

        String billNumber = generateBillNumber();
        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .patient(patient)
                .billingAccount(account)
                .opdVisit(visit)
                .billType(BillType.OPD)
                .billDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .status(BillStatus.GENERATED)
                .createdBy(username)
                .notes("OPD Visit Billing for Doctor Consultation")
                .items(new ArrayList<>())
                .build();

        Bill saved = billRepository.save(bill);

        // Add Doctor Consultation Charge
        BigDecimal consultFee = visit.getDoctor() != null && visit.getDoctor().getConsultationFee() != null
                ? visit.getDoctor().getConsultationFee() : new BigDecimal("500.00");

        BillItem consultationItem = BillItem.builder()
                .bill(saved)
                .chargeCode("CHG-CONSULT")
                .description("Doctor Consultation - Dr. " + (visit.getDoctor() != null ? visit.getDoctor().getFirstName() + " " + visit.getDoctor().getLastName() : "General Practitioner"))
                .sourceType(BillSourceType.CONSULTATION)
                .sourceId(opdVisitId)
                .quantity(1)
                .unitRate(consultFee)
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .lineTotal(consultFee)
                .build();

        saved.getItems().add(billItemRepository.save(consultationItem));

        calculateBillTotals(saved);
        Bill finalized = billRepository.save(saved);

        ledgerService.recordEntry(patient, finalized, null, null, null,
                LedgerEntryType.BILL, finalized.getGrandTotal(), BigDecimal.ZERO,
                "OPD Consultation Invoice: " + finalized.getBillNumber());

        auditLogService.logAction("GENERATE_OPD_BILL", "Bill", finalized.getId().toString(),
                "Generated OPD bill: " + finalized.getBillNumber());

        return mapToDto(finalized);
    }

    @Transactional
    public BillDto generatePharmacyBill(Long dispensingId, String username) {
        PharmacyDispensing dispensing = dispensingRepository.findById(dispensingId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispensing record not found with ID: " + dispensingId));

        if (billItemRepository.existsBySourceTypeAndSourceId(BillSourceType.PHARMACY, dispensingId)) {
            throw new DuplicateSourceBillingException("Pharmacy Dispensing ID " + dispensingId + " has already been billed.");
        }

        Patient patient = dispensing.getPatient();
        BillingAccount account = billingAccountService.getOrCreateAccountForPatient(patient.getId());

        String billNumber = generateBillNumber();
        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .patient(patient)
                .billingAccount(account)
                .billType(BillType.PHARMACY)
                .billDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .status(BillStatus.GENERATED)
                .createdBy(username)
                .notes("Pharmacy Dispensing Invoice")
                .items(new ArrayList<>())
                .build();

        Bill saved = billRepository.save(bill);

        BigDecimal unitRate = dispensing.getBatch().getSellingRate() != null ? dispensing.getBatch().getSellingRate() : new BigDecimal("10.00");
        BigDecimal lineTotal = unitRate.multiply(BigDecimal.valueOf(dispensing.getDispensedQuantity()));

        BillItem pharmacyItem = BillItem.builder()
                .bill(saved)
                .chargeCode("CHG-PHARMACY")
                .description(dispensing.getMedicine().getMedicineName() + " (Batch: " + dispensing.getBatch().getBatchNumber() + ")")
                .sourceType(BillSourceType.PHARMACY)
                .sourceId(dispensingId)
                .quantity(dispensing.getDispensedQuantity())
                .unitRate(unitRate)
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .lineTotal(lineTotal)
                .build();

        saved.getItems().add(billItemRepository.save(pharmacyItem));

        calculateBillTotals(saved);
        Bill finalized = billRepository.save(saved);

        ledgerService.recordEntry(patient, finalized, null, null, null,
                LedgerEntryType.BILL, finalized.getGrandTotal(), BigDecimal.ZERO,
                "Pharmacy Invoice: " + finalized.getBillNumber());

        auditLogService.logAction("GENERATE_PHARMACY_BILL", "Bill", finalized.getId().toString(),
                "Generated Pharmacy bill: " + finalized.getBillNumber());

        return mapToDto(finalized);
    }

    @Transactional
    public BillDto generateLabBill(Long labOrderId, String username) {
        LabOrder labOrder = labOrderRepository.findById(labOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Order not found with ID: " + labOrderId));

        if (billItemRepository.existsBySourceTypeAndSourceId(BillSourceType.LAB, labOrderId)) {
            throw new DuplicateSourceBillingException("Lab Order ID " + labOrderId + " has already been billed.");
        }

        Patient patient = labOrder.getPatient();
        BillingAccount account = billingAccountService.getOrCreateAccountForPatient(patient.getId());

        String billNumber = generateBillNumber();
        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .patient(patient)
                .billingAccount(account)
                .billType(BillType.LABORATORY)
                .billDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .status(BillStatus.GENERATED)
                .createdBy(username)
                .notes("Laboratory Order Invoice")
                .items(new ArrayList<>())
                .build();

        Bill saved = billRepository.save(bill);

        if (labOrder.getItems() != null) {
            for (LabOrderItem orderItem : labOrder.getItems()) {
                BigDecimal rate = new BigDecimal("350.00");
                BillItem labItem = BillItem.builder()
                        .bill(saved)
                        .chargeCode(orderItem.getLabTest().getTestCode())
                        .description(orderItem.getLabTest().getTestName())
                        .sourceType(BillSourceType.LAB)
                        .sourceId(labOrderId)
                        .quantity(1)
                        .unitRate(rate)
                        .discountPercentage(BigDecimal.ZERO)
                        .discountAmount(BigDecimal.ZERO)
                        .taxPercentage(BigDecimal.ZERO)
                        .taxAmount(BigDecimal.ZERO)
                        .lineTotal(rate)
                        .build();
                saved.getItems().add(billItemRepository.save(labItem));
            }
        }

        calculateBillTotals(saved);
        Bill finalized = billRepository.save(saved);

        ledgerService.recordEntry(patient, finalized, null, null, null,
                LedgerEntryType.BILL, finalized.getGrandTotal(), BigDecimal.ZERO,
                "Laboratory Invoice: " + finalized.getBillNumber());

        auditLogService.logAction("GENERATE_LAB_BILL", "Bill", finalized.getId().toString(),
                "Generated Lab bill: " + finalized.getBillNumber());

        return mapToDto(finalized);
    }

    @Transactional
    public BillDto generateRadiologyBill(Long radiologyOrderId, String username) {
        RadiologyOrder order = radiologyOrderRepository.findById(radiologyOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Radiology Order not found with ID: " + radiologyOrderId));

        if (billItemRepository.existsBySourceTypeAndSourceId(BillSourceType.RADIOLOGY, radiologyOrderId)) {
            throw new DuplicateSourceBillingException("Radiology Order ID " + radiologyOrderId + " has already been billed.");
        }

        Patient patient = order.getPatient();
        BillingAccount account = billingAccountService.getOrCreateAccountForPatient(patient.getId());

        String billNumber = generateBillNumber();
        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .patient(patient)
                .billingAccount(account)
                .billType(BillType.RADIOLOGY)
                .billDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .status(BillStatus.GENERATED)
                .createdBy(username)
                .notes("Radiology Order Invoice")
                .items(new ArrayList<>())
                .build();

        Bill saved = billRepository.save(bill);

        BigDecimal rate = new BigDecimal("800.00");
        BillItem radItem = BillItem.builder()
                .bill(saved)
                .chargeCode(order.getRadiologyTest().getTestCode())
                .description(order.getRadiologyTest().getTestName())
                .sourceType(BillSourceType.RADIOLOGY)
                .sourceId(radiologyOrderId)
                .quantity(1)
                .unitRate(rate)
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .lineTotal(rate)
                .build();

        saved.getItems().add(billItemRepository.save(radItem));

        calculateBillTotals(saved);
        Bill finalized = billRepository.save(saved);

        ledgerService.recordEntry(patient, finalized, null, null, null,
                LedgerEntryType.BILL, finalized.getGrandTotal(), BigDecimal.ZERO,
                "Radiology Invoice: " + finalized.getBillNumber());

        auditLogService.logAction("GENERATE_RADIOLOGY_BILL", "Bill", finalized.getId().toString(),
                "Generated Radiology bill: " + finalized.getBillNumber());

        return mapToDto(finalized);
    }

    @Transactional
    public BillDto generateIpdBill(Long ipdAdmissionId, String username) {
        IpdAdmission admission = ipdAdmissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD Admission not found with ID: " + ipdAdmissionId));

        Patient patient = admission.getPatient();
        BillingAccount account = billingAccountService.getOrCreateAccountForPatient(patient.getId());

        String billNumber = generateBillNumber();
        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .patient(patient)
                .billingAccount(account)
                .ipdAdmission(admission)
                .billType(BillType.FINAL_DISCHARGE)
                .billDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .status(BillStatus.GENERATED)
                .createdBy(username)
                .notes("IPD Final Discharge Summary Invoice")
                .items(new ArrayList<>())
                .build();

        Bill saved = billRepository.save(bill);

        // 1. Bed Charges calculation from BedAllocations
        List<BedAllocation> allocations = bedAllocationRepository.findByAdmissionIdOrderByStartDatetimeDesc(ipdAdmissionId);
        for (BedAllocation alloc : allocations) {
            LocalDateTime start = alloc.getStartDatetime();
            LocalDateTime end = alloc.getEndDatetime() != null ? alloc.getEndDatetime() : LocalDateTime.now();
            long days = Math.max(1, Duration.between(start, end).toDays());

            BigDecimal dailyRate = new BigDecimal("1500.00");
            BigDecimal totalBedCharge = dailyRate.multiply(BigDecimal.valueOf(days));

            BillItem bedItem = BillItem.builder()
                    .bill(saved)
                    .chargeCode("CHG-BED-" + alloc.getBed().getBedNumber())
                    .description("Bed Charge - Ward: " + alloc.getBed().getWard().getWardName() + ", Bed: " + alloc.getBed().getBedNumber() + " (" + days + " days)")
                    .sourceType(BillSourceType.BED)
                    .sourceId(alloc.getId())
                    .quantity((int) days)
                    .unitRate(dailyRate)
                    .discountPercentage(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.ZERO)
                    .taxPercentage(BigDecimal.ZERO)
                    .taxAmount(BigDecimal.ZERO)
                    .lineTotal(totalBedCharge)
                    .build();

            saved.getItems().add(billItemRepository.save(bedItem));
        }

        // 2. Doctor Inpatient Consultation Fee
        BigDecimal doctorFee = admission.getAdmittingDoctor() != null && admission.getAdmittingDoctor().getConsultationFee() != null
                ? admission.getAdmittingDoctor().getConsultationFee() : new BigDecimal("1000.00");

        BillItem doctorItem = BillItem.builder()
                .bill(saved)
                .chargeCode("CHG-IPD-DOCTOR")
                .description("Attending Specialist Consultation - Dr. " + (admission.getAdmittingDoctor() != null ? admission.getAdmittingDoctor().getFirstName() + " " + admission.getAdmittingDoctor().getLastName() : "Staff Physician"))
                .sourceType(BillSourceType.CONSULTATION)
                .sourceId(ipdAdmissionId)
                .quantity(1)
                .unitRate(doctorFee)
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .lineTotal(doctorFee)
                .build();

        saved.getItems().add(billItemRepository.save(doctorItem));

        // 3. Nursing & Service Charges
        BillItem nursingItem = BillItem.builder()
                .bill(saved)
                .chargeCode("CHG-NURSING")
                .description("24x7 Inpatient Nursing & Clinical Service Charges")
                .sourceType(BillSourceType.NURSING)
                .sourceId(ipdAdmissionId)
                .quantity(1)
                .unitRate(new BigDecimal("750.00"))
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .lineTotal(new BigDecimal("750.00"))
                .build();

        saved.getItems().add(billItemRepository.save(nursingItem));

        calculateBillTotals(saved);
        Bill finalized = billRepository.save(saved);

        ledgerService.recordEntry(patient, finalized, null, null, null,
                LedgerEntryType.BILL, finalized.getGrandTotal(), BigDecimal.ZERO,
                "IPD Final Discharge Invoice: " + finalized.getBillNumber());

        auditLogService.logAction("IPD_FINAL_BILL_GENERATED", "Bill", finalized.getId().toString(),
                "Generated IPD Final bill: " + finalized.getBillNumber() + " Total: ₹" + finalized.getGrandTotal());

        return mapToDto(finalized);
    }

    @Transactional
    public DischargeClearanceDto processDischargeClearance(Long ipdAdmissionId, String remarks, String username) {
        IpdAdmission admission = ipdAdmissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD Admission not found with ID: " + ipdAdmissionId));

        List<Bill> ipdBills = billRepository.findByIpdAdmissionId(ipdAdmissionId);
        Bill finalBill = ipdBills.stream().filter(b -> b.getBillType() == BillType.FINAL_DISCHARGE)
                .findFirst().orElse(null);

        if (finalBill == null) {
            // Generate final bill automatically if not exists
            BillDto billDto = generateIpdBill(ipdAdmissionId, username);
            finalBill = billRepository.findById(billDto.getId()).orElse(null);
        }

        BigDecimal outstanding = finalBill != null ? finalBill.getOutstandingAmount() : BigDecimal.ZERO;
        DischargeClearanceStatus status = outstanding.compareTo(BigDecimal.ZERO) == 0 ? DischargeClearanceStatus.CLEARED : DischargeClearanceStatus.PENDING;

        auditLogService.logAction("BILLING_CLEARANCE", "IpdAdmission", ipdAdmissionId.toString(),
                "Processed billing clearance status " + status + " for IPD Admission: " + admission.getAdmissionId());

        return DischargeClearanceDto.builder()
                .ipdAdmissionId(admission.getId())
                .admissionNumber(admission.getAdmissionId())
                .patientId(admission.getPatient().getId())
                .patientName(admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName())
                .finalBillId(finalBill != null ? finalBill.getId() : null)
                .finalBillNumber(finalBill != null ? finalBill.getBillNumber() : null)
                .grandTotal(finalBill != null ? finalBill.getGrandTotal() : BigDecimal.ZERO)
                .paidAmount(finalBill != null ? finalBill.getPaidAmount() : BigDecimal.ZERO)
                .outstandingAmount(outstanding)
                .clearanceStatus(status)
                .remarks(remarks)
                .clearedBy(username)
                .build();
    }

    // Helper Calculation Functions

    private void addBillItemToBill(Bill bill, BillItemDto dto) {
        BigDecimal unitRate = dto.getUnitRate() != null ? dto.getUnitRate() : BigDecimal.ZERO;
        int qty = dto.getQuantity() != null ? dto.getQuantity() : 1;
        BigDecimal gross = unitRate.multiply(BigDecimal.valueOf(qty));

        BigDecimal discPct = dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : BigDecimal.ZERO;
        BigDecimal discAmt = dto.getDiscountAmount() != null ? dto.getDiscountAmount() : gross.multiply(discPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal taxable = gross.subtract(discAmt);
        if (taxable.compareTo(BigDecimal.ZERO) < 0) taxable = BigDecimal.ZERO;

        BigDecimal taxPct = dto.getTaxPercentage() != null ? dto.getTaxPercentage() : BigDecimal.ZERO;
        BigDecimal taxAmt = taxable.multiply(taxPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal lineTotal = taxable.add(taxAmt);

        BillItem item = BillItem.builder()
                .bill(bill)
                .chargeCode(dto.getChargeCode())
                .description(dto.getDescription())
                .sourceType(dto.getSourceType())
                .sourceId(dto.getSourceId())
                .quantity(qty)
                .unitRate(unitRate)
                .discountPercentage(discPct)
                .discountAmount(discAmt)
                .taxPercentage(taxPct)
                .taxAmount(taxAmt)
                .lineTotal(lineTotal)
                .build();

        bill.getItems().add(billItemRepository.save(item));
    }

    public void calculateBillTotals(Bill bill) {
        if (bill.getItems() == null || bill.getItems().isEmpty()) {
            bill.setSubtotal(BigDecimal.ZERO);
            bill.setDiscountAmount(BigDecimal.ZERO);
            bill.setTaxAmount(BigDecimal.ZERO);
            bill.setRoundOff(BigDecimal.ZERO);
            bill.setGrandTotal(BigDecimal.ZERO);
            bill.setOutstandingAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (BillItem item : bill.getItems()) {
            BigDecimal lineGross = item.getUnitRate().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineGross);
            totalDiscount = totalDiscount.add(item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO);
            totalTax = totalTax.add(item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO);
        }

        BigDecimal rawTotal = subtotal.subtract(totalDiscount).add(totalTax);
        BigDecimal roundedTotal = rawTotal.setScale(0, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
        BigDecimal roundOff = roundedTotal.subtract(rawTotal);

        bill.setSubtotal(subtotal);
        bill.setDiscountAmount(totalDiscount);
        bill.setTaxAmount(totalTax);
        bill.setRoundOff(roundOff);
        bill.setGrandTotal(roundedTotal);

        BigDecimal paid = bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal refunded = bill.getRefundedAmount() != null ? bill.getRefundedAmount() : BigDecimal.ZERO;
        BigDecimal outstanding = roundedTotal.subtract(paid).add(refunded);
        if (outstanding.compareTo(BigDecimal.ZERO) < 0) outstanding = BigDecimal.ZERO;

        bill.setOutstandingAmount(outstanding);

        // Update status based on payment progress
        if (bill.getStatus() != BillStatus.DRAFT && bill.getStatus() != BillStatus.CANCELLED) {
            if (outstanding.compareTo(BigDecimal.ZERO) == 0 && paid.compareTo(BigDecimal.ZERO) > 0) {
                bill.setStatus(BillStatus.PAID);
            } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
                bill.setStatus(BillStatus.PARTIALLY_PAID);
            } else {
                bill.setStatus(BillStatus.GENERATED);
            }
        }
    }

    private String generateBillNumber() {
        long count = billRepository.count() + 1;
        return String.format("BILL-%d-%06d", LocalDate.now().getYear(), count);
    }

    public BillDto mapToDto(Bill b) {
        if (b == null) return null;
        List<BillItemDto> itemDtos = b.getItems() != null ? b.getItems().stream().map(this::mapItemToDto).collect(Collectors.toList()) : new ArrayList<>();

        return BillDto.builder()
                .id(b.getId())
                .billNumber(b.getBillNumber())
                .patientId(b.getPatient().getId())
                .patientName(b.getPatient().getFirstName() + " " + b.getPatient().getLastName())
                .patientCode(b.getPatient().getPatientId())
                .billingAccountId(b.getBillingAccount() != null ? b.getBillingAccount().getId() : null)
                .opdVisitId(b.getOpdVisit() != null ? b.getOpdVisit().getId() : null)
                .ipdAdmissionId(b.getIpdAdmission() != null ? b.getIpdAdmission().getId() : null)
                .billType(b.getBillType())
                .billDate(b.getBillDate())
                .dueDate(b.getDueDate())
                .subtotal(b.getSubtotal())
                .discountAmount(b.getDiscountAmount())
                .discountReason(b.getDiscountReason())
                .discountApprovedBy(b.getDiscountApprovedBy())
                .taxAmount(b.getTaxAmount())
                .roundOff(b.getRoundOff())
                .grandTotal(b.getGrandTotal())
                .paidAmount(b.getPaidAmount())
                .refundedAmount(b.getRefundedAmount())
                .outstandingAmount(b.getOutstandingAmount())
                .status(b.getStatus())
                .createdBy(b.getCreatedBy())
                .notes(b.getNotes())
                .items(itemDtos)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    public BillItemDto mapItemToDto(BillItem i) {
        if (i == null) return null;
        return BillItemDto.builder()
                .id(i.getId())
                .chargeCode(i.getChargeCode())
                .description(i.getDescription())
                .sourceType(i.getSourceType())
                .sourceId(i.getSourceId())
                .quantity(i.getQuantity())
                .unitRate(i.getUnitRate())
                .discountPercentage(i.getDiscountPercentage())
                .discountAmount(i.getDiscountAmount())
                .taxPercentage(i.getTaxPercentage())
                .taxAmount(i.getTaxAmount())
                .lineTotal(i.getLineTotal())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
