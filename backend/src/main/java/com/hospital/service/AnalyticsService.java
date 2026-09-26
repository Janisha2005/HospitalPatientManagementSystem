package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabOrderRepository labOrderRepository;
    private final RadiologyOrderRepository radiologyOrderRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final PharmacyDispensingRepository dispensingRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    // Helper date bounds resolver
    public LocalDateTime[] resolveDateRange(String period, String customFrom, String customTo) {
        LocalDate today = LocalDate.now(IST_ZONE);
        LocalDate startDate;
        LocalDate endDate;

        if ("YESTERDAY".equalsIgnoreCase(period)) {
            startDate = today.minusDays(1);
            endDate = today.minusDays(1);
        } else if ("LAST_7_DAYS".equalsIgnoreCase(period)) {
            startDate = today.minusDays(6);
            endDate = today;
        } else if ("LAST_30_DAYS".equalsIgnoreCase(period)) {
            startDate = today.minusDays(29);
            endDate = today;
        } else if ("CURRENT_MONTH".equalsIgnoreCase(period)) {
            startDate = today.withDayOfMonth(1);
            endDate = today;
        } else if ("PREVIOUS_MONTH".equalsIgnoreCase(period)) {
            LocalDate prevMonth = today.minusMonths(1);
            startDate = prevMonth.withDayOfMonth(1);
            endDate = prevMonth.withDayOfMonth(prevMonth.lengthOfMonth());
        } else if ("CURRENT_YEAR".equalsIgnoreCase(period)) {
            startDate = today.withDayOfYear(1);
            endDate = today;
        } else if ("CUSTOM".equalsIgnoreCase(period) && customFrom != null && customTo != null) {
            startDate = LocalDate.parse(customFrom);
            endDate = LocalDate.parse(customTo);
        } else { // Default TODAY
            startDate = today;
            endDate = today;
        }

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.plusDays(1).atStartOfDay(); // exclusive end
        return new LocalDateTime[]{startDt, endDt};
    }

    // 1. Executive Analytics
    public ExecutiveAnalyticsDto getExecutiveAnalytics(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        long totalRegistered = patientRepository.count();
        long newPatients = patientRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(startDt) && p.getCreatedAt().isBefore(endDt))
                .count();
        long returningPatients = Math.max(0, totalRegistered - newPatients);

        long opdVisits = opdVisitRepository.findAll().stream()
                .filter(v -> v.getVisitDate() != null)
                .filter(v -> {
                    LocalDateTime dt = v.getVisitDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .count();

        long ipdAdmissions = ipdAdmissionRepository.findAll().stream()
                .filter(a -> a.getAdmissionDate() != null)
                .filter(a -> {
                    LocalDateTime dt = a.getAdmissionDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .count();

        long discharges = ipdAdmissionRepository.findAll().stream()
                .filter(a -> a.getActualDischargeDate() != null)
                .filter(a -> {
                    LocalDateTime dt = a.getActualDischargeDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .count();

        // Appointments
        List<Appointment> appts = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDate() != null)
                .filter(a -> {
                    LocalDateTime dt = a.getAppointmentDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .collect(Collectors.toList());

        long totalAppts = appts.size();
        long scheduled = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED).count();
        long confirmed = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED).count();
        long checkedIn = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.CHECKED_IN).count();
        long completed = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long cancelled = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        long noShow = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();

        double completionPct = totalAppts > 0 ? ((double) completed / totalAppts) * 100.0 : 0.0;
        double cancellationPct = totalAppts > 0 ? ((double) cancelled / totalAppts) * 100.0 : 0.0;
        double noShowPct = totalAppts > 0 ? ((double) noShow / totalAppts) * 100.0 : 0.0;

        // Clinical
        long labOrders = labOrderRepository.findAll().stream()
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> {
                    LocalDateTime dt = o.getOrderDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .count();

        long completedLab = labOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == LabOrderStatus.COMPLETED || o.getStatus() == LabOrderStatus.VERIFIED)
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> {
                    LocalDateTime dt = o.getOrderDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .count();

        long radOrders = radiologyOrderRepository.findAll().stream()
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> {
                    LocalDateTime dt = o.getOrderDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .count();

        long completedRad = radiologyOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == RadiologyOrderStatus.COMPLETED)
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> {
                    LocalDateTime dt = o.getOrderDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .count();

        long prescriptions = prescriptionRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(startDt) && p.getCreatedAt().isBefore(endDt))
                .count();

        // Pharmacy
        List<PharmacyDispensing> dispensings = dispensingRepository.findAll().stream()
                .filter(d -> d.getDispensedDatetime() != null && !d.getDispensedDatetime().isBefore(startDt) && d.getDispensedDatetime().isBefore(endDt))
                .collect(Collectors.toList());

        long dispensingTxCount = dispensings.size();
        long totalMedicinesDispensed = dispensings.stream().mapToLong(d -> d.getDispensedQuantity() != null ? d.getDispensedQuantity() : 0).sum();
        BigDecimal pharmacyRev = dispensings.stream()
                .map(d -> {
                    if (d.getBatch() != null && d.getBatch().getMrp() != null && d.getDispensedQuantity() != null) {
                        return d.getBatch().getMrp().multiply(BigDecimal.valueOf(d.getDispensedQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lowStockCount = medicineRepository.findAll().stream().filter(m -> {
            long totalStock = medicineBatchRepository.findByMedicineId(m.getId()).stream().mapToLong(MedicineBatch::getQuantityAvailable).sum();
            return m.getReorderLevel() != null && totalStock <= m.getReorderLevel();
        }).count();
        LocalDate todayDate = LocalDate.now(IST_ZONE);
        long nearExpiryCount = medicineBatchRepository.findAll().stream().filter(b -> b.getExpiryDate() != null && !b.getExpiryDate().isBefore(todayDate) && b.getExpiryDate().isBefore(todayDate.plusDays(30))).count();
        long expiredCount = medicineBatchRepository.findAll().stream().filter(b -> b.getExpiryDate() != null && b.getExpiryDate().isBefore(todayDate)).count();

        // Financials
        List<Bill> billsInPeriod = billRepository.findAll().stream()
                .filter(b -> b.getBillDate() != null)
                .filter(b -> {
                    LocalDateTime dt = b.getBillDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .filter(b -> b.getStatus() != BillStatus.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal totalBilled = billsInPeriod.stream().map(b -> b.getGrandTotal() != null ? b.getGrandTotal() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<Payment> paymentsInPeriod = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().isBefore(startDt) && p.getPaymentDate().isBefore(endDt))
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());

        BigDecimal totalCollected = paymentsInPeriod.stream().map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Refund> refundsInPeriod = refundRepository.findAll().stream()
                .filter(r -> r.getRefundDate() != null && !r.getRefundDate().isBefore(startDt) && r.getRefundDate().isBefore(endDt))
                .filter(r -> r.getStatus() == RefundStatus.PROCESSED)
                .collect(Collectors.toList());

        BigDecimal totalRefunded = refundsInPeriod.stream().map(r -> r.getRefundAmount() != null ? r.getRefundAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CreditNote> creditNotesInPeriod = creditNoteRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().isBefore(startDt) && c.getCreatedAt().isBefore(endDt))
                .filter(c -> c.getStatus() == CreditNoteStatus.APPROVED)
                .collect(Collectors.toList());

        BigDecimal totalCreditNotes = creditNotesInPeriod.stream().map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = billRepository.findAll().stream()
                .filter(b -> b.getStatus() != BillStatus.CANCELLED && b.getStatus() != BillStatus.PAID)
                .map(b -> b.getOutstandingAmount() != null ? b.getOutstandingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netCollected = totalCollected.subtract(totalRefunded);

        // Beds
        List<Bed> allBeds = bedRepository.findAll();
        long totalBeds = allBeds.size();
        long availableBeds = allBeds.stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();
        long occupiedBeds = allBeds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
        long cleaningBeds = allBeds.stream().filter(b -> b.getStatus() == BedStatus.CLEANING || b.getStatus() == BedStatus.RESERVED || b.getStatus() == BedStatus.MAINTENANCE).count();
        double bedOccPct = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100.0 : 0.0;

        return ExecutiveAnalyticsDto.builder()
                .totalRegisteredPatients(totalRegistered)
                .newPatients(newPatients)
                .returningPatients(returningPatients)
                .opdPatientsCount(opdVisits)
                .ipdAdmissionsCount(ipdAdmissions)
                .dischargesCount(discharges)
                .totalAppointments(totalAppts)
                .scheduledAppointments(scheduled)
                .confirmedAppointments(confirmed)
                .checkedInAppointments(checkedIn)
                .completedAppointments(completed)
                .cancelledAppointments(cancelled)
                .noShowAppointments(noShow)
                .completionPercentage(round2(completionPct))
                .cancellationPercentage(round2(cancellationPct))
                .noShowPercentage(round2(noShowPct))
                .opdVisitsCount(opdVisits)
                .labOrdersCount(labOrders)
                .completedLabTestsCount(completedLab)
                .radiologyOrdersCount(radOrders)
                .completedRadiologyStudiesCount(completedRad)
                .prescriptionsIssuedCount(prescriptions)
                .medicinesDispensedCount(totalMedicinesDispensed)
                .dispensingTransactionsCount(dispensingTxCount)
                .pharmacyRevenue(pharmacyRev.setScale(2, RoundingMode.HALF_UP))
                .lowStockMedicinesCount(lowStockCount)
                .nearExpiryBatchesCount(nearExpiryCount)
                .expiredBatchesCount(expiredCount)
                .totalBilled(totalBilled.setScale(2, RoundingMode.HALF_UP))
                .totalCollected(totalCollected.setScale(2, RoundingMode.HALF_UP))
                .totalOutstanding(totalOutstanding.setScale(2, RoundingMode.HALF_UP))
                .totalRefunded(totalRefunded.setScale(2, RoundingMode.HALF_UP))
                .totalCreditNotes(totalCreditNotes.setScale(2, RoundingMode.HALF_UP))
                .netCollected(netCollected.setScale(2, RoundingMode.HALF_UP))
                .totalBeds(totalBeds)
                .availableBeds(availableBeds)
                .occupiedBeds(occupiedBeds)
                .cleaningBeds(cleaningBeds)
                .bedOccupancyPercentage(round2(bedOccPct))
                .build();
    }

    // 2. OPD Analytics
    public OPDAnalyticsDto getOPDAnalytics(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        List<OpdVisit> visits = opdVisitRepository.findAll().stream()
                .filter(v -> v.getVisitDate() != null)
                .filter(v -> {
                    LocalDateTime dt = v.getVisitDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .collect(Collectors.toList());

        long totalVisits = visits.size();
        long uniquePatients = visits.stream().map(v -> v.getPatient().getId()).distinct().count();
        long newPatients = visits.stream().filter(v -> v.getAppointment() != null && v.getAppointment().getAppointmentType() == AppointmentType.NEW_CONSULTATION).count();
        long returningPatients = Math.max(0, totalVisits - newPatients);

        long daysCount = Math.max(1, ChronoUnit.DAYS.between(startDt.toLocalDate(), endDt.toLocalDate()));
        double avgVisitsPerDay = (double) totalVisits / daysCount;

        List<Appointment> appts = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDate() != null)
                .filter(a -> {
                    LocalDateTime dt = a.getAppointmentDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .collect(Collectors.toList());

        long totalAppts = appts.size();
        long noShows = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
        double noShowRate = totalAppts > 0 ? ((double) noShows / totalAppts) * 100.0 : 0.0;

        Map<String, Long> visitsByDept = visits.stream()
                .filter(v -> v.getDepartment() != null)
                .collect(Collectors.groupingBy(v -> v.getDepartment().getDepartmentName(), Collectors.counting()));

        Map<String, Long> visitsByDoctor = visits.stream()
                .filter(v -> v.getDoctor() != null)
                .collect(Collectors.groupingBy(v -> (v.getDoctor().getFirstName() + " " + v.getDoctor().getLastName()), Collectors.counting()));

        Map<String, Long> visitsByStatus = visits.stream()
                .collect(Collectors.groupingBy(v -> v.getVisitStatus() != null ? v.getVisitStatus().name() : "UNKNOWN", Collectors.counting()));

        Map<String, Long> visitsByType = visits.stream()
                .filter(v -> v.getAppointment() != null && v.getAppointment().getAppointmentType() != null)
                .collect(Collectors.groupingBy(v -> v.getAppointment().getAppointmentType().name(), Collectors.counting()));

        // Daily trend
        Map<String, List<OpdVisit>> groupedByDate = visits.stream()
                .collect(Collectors.groupingBy(v -> v.getVisitDate().toString()));

        List<OPDAnalyticsDto.DailyTrendDto> dailyTrend = groupedByDate.entrySet().stream()
                .map(e -> OPDAnalyticsDto.DailyTrendDto.builder()
                        .date(e.getKey())
                        .count(e.getValue().size())
                        .revenue(e.getValue().size() * 500.0) // Nominal standard consultation rate
                        .build())
                .sorted(Comparator.comparing(OPDAnalyticsDto.DailyTrendDto::getDate))
                .collect(Collectors.toList());

        return OPDAnalyticsDto.builder()
                .totalVisits(totalVisits)
                .uniquePatients(uniquePatients)
                .newPatients(newPatients)
                .returningPatients(returningPatients)
                .averageVisitsPerDay(round2(avgVisitsPerDay))
                .noShowRate(round2(noShowRate))
                .visitsByDepartment(visitsByDept)
                .visitsByDoctor(visitsByDoctor)
                .visitsByStatus(visitsByStatus)
                .visitsByAppointmentType(visitsByType)
                .dailyTrend(dailyTrend)
                .build();
    }

    // 3. IPD Analytics
    public IPDAnalyticsDto getIPDAnalytics(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        List<IpdAdmission> admissions = ipdAdmissionRepository.findAll();
        
        List<IpdAdmission> admissionsInPeriod = admissions.stream()
                .filter(a -> a.getAdmissionDate() != null)
                .filter(a -> {
                    LocalDateTime dt = a.getAdmissionDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .collect(Collectors.toList());

        long totalAdmissions = admissionsInPeriod.size();
        long currentAdmissions = admissions.stream().filter(a -> a.getStatus() == AdmissionStatus.ADMITTED).count();

        List<IpdAdmission> dischargesInPeriod = admissions.stream()
                .filter(a -> a.getActualDischargeDate() != null)
                .filter(a -> {
                    LocalDateTime dt = a.getActualDischargeDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .collect(Collectors.toList());

        long totalDischarges = dischargesInPeriod.size();

        double totalStayDays = dischargesInPeriod.stream()
                .mapToDouble(a -> {
                    long days = ChronoUnit.DAYS.between(a.getAdmissionDate(), a.getActualDischargeDate());
                    return Math.max(1.0, (double) days);
                }).sum();

        double avgStay = totalDischarges > 0 ? totalStayDays / totalDischarges : 0.0;

        List<Bed> allBeds = bedRepository.findAll();
        long totalBeds = allBeds.size();
        long availableBeds = allBeds.stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();
        long occupiedBeds = allBeds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
        long cleaningBeds = allBeds.stream().filter(b -> b.getStatus() == BedStatus.CLEANING || b.getStatus() == BedStatus.RESERVED || b.getStatus() == BedStatus.MAINTENANCE).count();
        double bedOccPct = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100.0 : 0.0;

        Map<String, Long> admByWard = admissionsInPeriod.stream()
                .filter(a -> a.getWard() != null)
                .collect(Collectors.groupingBy(a -> a.getWard().getWardName(), Collectors.counting()));

        Map<String, Long> disByWard = dischargesInPeriod.stream()
                .filter(a -> a.getWard() != null)
                .collect(Collectors.groupingBy(a -> a.getWard().getWardName(), Collectors.counting()));

        Map<String, Long> admByStatus = admissions.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus() != null ? a.getStatus().name() : "UNKNOWN", Collectors.counting()));

        Map<String, Long> clearanceStatus = admissions.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus() != null ? a.getStatus().name() : "NONE", Collectors.counting()));

        long totalTransfers = bedAllocationRepository.findAll().stream()
                .filter(b -> b.getStartDatetime() != null && !b.getStartDatetime().isBefore(startDt) && b.getStartDatetime().isBefore(endDt))
                .count();

        return IPDAnalyticsDto.builder()
                .totalAdmissions(totalAdmissions)
                .currentAdmissions(currentAdmissions)
                .totalDischarges(totalDischarges)
                .averageLengthOfStayDays(round2(avgStay))
                .bedOccupancyPercentage(round2(bedOccPct))
                .totalBeds(totalBeds)
                .availableBeds(availableBeds)
                .occupiedBeds(occupiedBeds)
                .cleaningBeds(cleaningBeds)
                .admissionsByWard(admByWard)
                .dischargesByWard(disByWard)
                .admissionsByStatus(admByStatus)
                .clearanceStatusBreakdown(clearanceStatus)
                .totalTransfersCount(totalTransfers)
                .build();
    }

    // 4. Bed Occupancy Report
    public BedOccupancyReportDto getBedOccupancyReport() {
        List<Bed> allBeds = bedRepository.findAll();
        long totalBeds = allBeds.size();
        long available = allBeds.stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();
        long occupied = allBeds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
        long cleaning = allBeds.stream().filter(b -> b.getStatus() == BedStatus.CLEANING).count();
        long maintenance = allBeds.stream().filter(b -> b.getStatus() == BedStatus.MAINTENANCE || b.getStatus() == BedStatus.RESERVED).count();
        double overallOcc = totalBeds > 0 ? ((double) occupied / totalBeds) * 100.0 : 0.0;

        List<Ward> wards = wardRepository.findAll();
        List<BedOccupancyReportDto.WardOccupancyDto> wardOccupancies = wards.stream().map(w -> {
            List<Bed> wBeds = allBeds.stream().filter(b -> b.getWard() != null && b.getWard().getId().equals(w.getId())).collect(Collectors.toList());
            long wTotal = wBeds.size();
            long wAvail = wBeds.stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();
            long wOcc = wBeds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
            long wClean = wBeds.stream().filter(b -> b.getStatus() == BedStatus.CLEANING).count();
            double wOccPct = wTotal > 0 ? ((double) wOcc / wTotal) * 100.0 : 0.0;

            return BedOccupancyReportDto.WardOccupancyDto.builder()
                    .wardId(w.getId())
                    .wardName(w.getWardName())
                    .wardType(w.getWardType() != null ? w.getWardType().name() : "GENERAL")
                    .totalBeds(wTotal)
                    .availableBeds(wAvail)
                    .occupiedBeds(wOcc)
                    .cleaningBeds(wClean)
                    .occupancyPercentage(round2(wOccPct))
                    .dailyRate(500.0) // Nominal ward rate
                    .build();
        }).collect(Collectors.toList());

        return BedOccupancyReportDto.builder()
                .totalBeds(totalBeds)
                .availableBeds(available)
                .occupiedBeds(occupied)
                .cleaningBeds(cleaning)
                .maintenanceBeds(maintenance)
                .overallOccupancyPercentage(round2(overallOcc))
                .wardOccupancies(wardOccupancies)
                .build();
    }

    // 5. Payment Collection Report
    public PaymentCollectionReportDto getPaymentCollectionReport(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().isBefore(startDt) && p.getPaymentDate().isBefore(endDt))
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());

        BigDecimal totalCollected = payments.stream().map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalTx = payments.size();

        Map<String, List<Payment>> groupedByMethod = payments.stream()
                .collect(Collectors.groupingBy(p -> p.getPaymentMethod() != null ? p.getPaymentMethod().name() : "CASH"));

        Map<String, PaymentCollectionReportDto.MethodBreakdownDto> methodBreakdown = new LinkedHashMap<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            List<Payment> methodPayments = groupedByMethod.getOrDefault(method.name(), Collections.emptyList());
            BigDecimal amount = methodPayments.stream().map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            long count = methodPayments.size();
            double pct = totalCollected.compareTo(BigDecimal.ZERO) > 0 ? amount.divide(totalCollected, 4, RoundingMode.HALF_UP).doubleValue() * 100.0 : 0.0;

            methodBreakdown.put(method.name(), PaymentCollectionReportDto.MethodBreakdownDto.builder()
                    .paymentMethod(method.name())
                    .amount(amount.setScale(2, RoundingMode.HALF_UP))
                    .count(count)
                    .percentageOfTotal(round2(pct))
                    .build());
        }

        // Daily collection trend
        Map<String, List<Payment>> groupedByDate = payments.stream()
                .collect(Collectors.groupingBy(p -> p.getPaymentDate().toLocalDate().toString()));

        List<PaymentCollectionReportDto.DailyCollectionDto> trend = groupedByDate.entrySet().stream()
                .map(e -> {
                    BigDecimal dTotal = e.getValue().stream().map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                    Map<String, BigDecimal> mAmounts = e.getValue().stream()
                            .collect(Collectors.groupingBy(p -> p.getPaymentMethod().name(),
                                    Collectors.reducing(BigDecimal.ZERO, p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO, BigDecimal::add)));

                    return PaymentCollectionReportDto.DailyCollectionDto.builder()
                            .date(e.getKey())
                            .totalAmount(dTotal.setScale(2, RoundingMode.HALF_UP))
                            .transactionCount(e.getValue().size())
                            .methodAmounts(mAmounts)
                            .build();
                })
                .sorted(Comparator.comparing(PaymentCollectionReportDto.DailyCollectionDto::getDate))
                .collect(Collectors.toList());

        return PaymentCollectionReportDto.builder()
                .totalCollectedAmount(totalCollected.setScale(2, RoundingMode.HALF_UP))
                .totalTransactionsCount(totalTx)
                .methodBreakdown(methodBreakdown)
                .dailyCollectionTrend(trend)
                .build();
    }

    // 6. Outstanding / Receivable Report
    public OutstandingReportDto getOutstandingReport() {
        List<Bill> unpaidBills = billRepository.findAll().stream()
                .filter(b -> b.getStatus() != BillStatus.CANCELLED && b.getStatus() != BillStatus.PAID)
                .filter(b -> b.getOutstandingAmount() != null && b.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        BigDecimal totalOutstanding = unpaidBills.stream().map(Bill::getOutstandingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalUnpaidCount = unpaidBills.size();

        LocalDate today = LocalDate.now(IST_ZONE);

        BigDecimal currentBucket = BigDecimal.ZERO;
        BigDecimal bucket31To60 = BigDecimal.ZERO;
        BigDecimal bucket61To90 = BigDecimal.ZERO;
        BigDecimal bucketOver90 = BigDecimal.ZERO;

        List<OutstandingReportDto.OutstandingBillItemDto> items = new ArrayList<>();

        for (Bill b : unpaidBills) {
            LocalDate billDate = b.getBillDate() != null ? b.getBillDate() : today;
            long ageDays = Math.max(0, ChronoUnit.DAYS.between(billDate, today));

            String bucket;
            if (ageDays <= 30) {
                bucket = "CURRENT (0-30 days)";
                currentBucket = currentBucket.add(b.getOutstandingAmount());
            } else if (ageDays <= 60) {
                bucket = "31-60 days";
                bucket31To60 = bucket31To60.add(b.getOutstandingAmount());
            } else if (ageDays <= 90) {
                bucket = "61-90 days";
                bucket61To90 = bucket61To90.add(b.getOutstandingAmount());
            } else {
                bucket = "90+ days";
                bucketOver90 = bucketOver90.add(b.getOutstandingAmount());
            }

            items.add(OutstandingReportDto.OutstandingBillItemDto.builder()
                    .billId(b.getId())
                    .billNumber(b.getBillNumber())
                    .billDate(b.getBillDate() != null ? b.getBillDate().toString() : "")
                    .patientId(b.getPatient() != null ? b.getPatient().getId() : null)
                    .patientName(b.getPatient() != null ? (b.getPatient().getFirstName() + " " + b.getPatient().getLastName()) : "N/A")
                    .patientCode(b.getPatient() != null ? b.getPatient().getPatientId() : "")
                    .billType(b.getBillType() != null ? b.getBillType().name() : "GENERAL")
                    .status(b.getStatus() != null ? b.getStatus().name() : "")
                    .totalAmount(b.getGrandTotal() != null ? b.getGrandTotal() : BigDecimal.ZERO)
                    .paidAmount(b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO)
                    .outstandingAmount(b.getOutstandingAmount())
                    .ageDays(ageDays)
                    .agingBucket(bucket)
                    .build());
        }

        return OutstandingReportDto.builder()
                .totalOutstandingAmount(totalOutstanding.setScale(2, RoundingMode.HALF_UP))
                .totalUnpaidBillsCount(totalUnpaidCount)
                .currentBucketAmount(currentBucket.setScale(2, RoundingMode.HALF_UP))
                .bucket31To60Amount(bucket31To60.setScale(2, RoundingMode.HALF_UP))
                .bucket61To90Amount(bucket61To90.setScale(2, RoundingMode.HALF_UP))
                .bucketOver90Amount(bucketOver90.setScale(2, RoundingMode.HALF_UP))
                .outstandingBills(items)
                .build();
    }

    // 7. Pharmacy Analytics
    public PharmacyAnalyticsDto getPharmacyAnalytics(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        List<PharmacyDispensing> dispensings = dispensingRepository.findAll().stream()
                .filter(d -> d.getDispensedDatetime() != null && !d.getDispensedDatetime().isBefore(startDt) && d.getDispensedDatetime().isBefore(endDt))
                .collect(Collectors.toList());

        long txCount = dispensings.size();
        long totalMedCount = dispensings.stream().mapToLong(d -> d.getDispensedQuantity() != null ? d.getDispensedQuantity() : 0).sum();
        BigDecimal rev = dispensings.stream()
                .map(d -> {
                    if (d.getBatch() != null && d.getBatch().getMrp() != null && d.getDispensedQuantity() != null) {
                        return d.getBatch().getMrp().multiply(BigDecimal.valueOf(d.getDispensedQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lowStock = medicineRepository.findAll().stream().filter(m -> {
            long totalStock = medicineBatchRepository.findByMedicineId(m.getId()).stream().mapToLong(MedicineBatch::getQuantityAvailable).sum();
            return m.getReorderLevel() != null && totalStock <= m.getReorderLevel();
        }).count();
        LocalDate todayDate = LocalDate.now(IST_ZONE);
        long nearExpiry = medicineBatchRepository.findAll().stream().filter(b -> b.getExpiryDate() != null && !b.getExpiryDate().isBefore(todayDate) && b.getExpiryDate().isBefore(todayDate.plusDays(30))).count();
        long expired = medicineBatchRepository.findAll().stream().filter(b -> b.getExpiryDate() != null && b.getExpiryDate().isBefore(todayDate)).count();

        // Top dispensed medicines
        Map<Medicine, Long> medQuantities = new HashMap<>();
        Map<Medicine, BigDecimal> medRevenues = new HashMap<>();

        for (PharmacyDispensing d : dispensings) {
            if (d.getMedicine() != null) {
                long qty = d.getDispensedQuantity() != null ? d.getDispensedQuantity() : 0;
                medQuantities.put(d.getMedicine(), medQuantities.getOrDefault(d.getMedicine(), 0L) + qty);
                BigDecimal itemTotal = (d.getBatch() != null && d.getBatch().getMrp() != null) ? d.getBatch().getMrp().multiply(BigDecimal.valueOf(qty)) : BigDecimal.ZERO;
                medRevenues.put(d.getMedicine(), medRevenues.getOrDefault(d.getMedicine(), BigDecimal.ZERO).add(itemTotal));
            }
        }

        List<PharmacyAnalyticsDto.TopDispensedMedicineDto> topMeds = medQuantities.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .map(e -> PharmacyAnalyticsDto.TopDispensedMedicineDto.builder()
                        .medicineId(e.getKey().getId())
                        .medicineName(e.getKey().getMedicineName())
                        .categoryName(e.getKey().getCategory() != null ? e.getKey().getCategory().getCategoryName() : "GENERAL")
                        .quantityDispensed(e.getValue())
                        .totalRevenue(medRevenues.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
                        .build())
                .collect(Collectors.toList());

        return PharmacyAnalyticsDto.builder()
                .dispensingTransactionsCount(txCount)
                .totalMedicinesDispensedCount(totalMedCount)
                .pharmacyRevenue(rev.setScale(2, RoundingMode.HALF_UP))
                .lowStockMedicinesCount(lowStock)
                .nearExpiryBatchesCount(nearExpiry)
                .expiredBatchesCount(expired)
                .pendingPurchaseOrdersCount(0L)
                .patientReturnsCount(0L)
                .topDispensedMedicines(topMeds)
                .build();
    }

    // 8. Laboratory Analytics
    public LaboratoryAnalyticsDto getLaboratoryAnalytics(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        List<LabOrder> orders = labOrderRepository.findAll().stream()
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> {
                    LocalDateTime dt = o.getOrderDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .collect(Collectors.toList());

        long totalOrders = orders.size();
        long samples = orders.stream().filter(o -> o.getStatus() == LabOrderStatus.SAMPLE_COLLECTED || o.getStatus() == LabOrderStatus.COMPLETED || o.getStatus() == LabOrderStatus.VERIFIED).count();
        long completed = orders.stream().filter(o -> o.getStatus() == LabOrderStatus.COMPLETED || o.getStatus() == LabOrderStatus.VERIFIED).count();
        long pending = orders.stream().filter(o -> o.getStatus() == LabOrderStatus.ORDERED || o.getStatus() == LabOrderStatus.SAMPLE_PENDING || o.getStatus() == LabOrderStatus.SAMPLE_COLLECTED).count();
        long verified = orders.stream().filter(o -> o.getStatus() == LabOrderStatus.VERIFIED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == LabOrderStatus.CANCELLED).count();

        Map<String, Long> statusMap = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus() != null ? o.getStatus().name() : "UNKNOWN", Collectors.counting()));

        Map<String, Long> priorityMap = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getPriority() != null ? o.getPriority().name() : "ROUTINE", Collectors.counting()));

        return LaboratoryAnalyticsDto.builder()
                .totalOrders(totalOrders)
                .samplesCollected(samples)
                .testsCompleted(completed)
                .pendingTests(pending)
                .verifiedResults(verified)
                .cancelledOrders(cancelled)
                .labRevenue(BigDecimal.valueOf(completed * 350.0).setScale(2, RoundingMode.HALF_UP))
                .ordersByStatus(statusMap)
                .ordersByPriority(priorityMap)
                .build();
    }

    // 9. Radiology Analytics
    public RadiologyAnalyticsDto getRadiologyAnalytics(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        List<RadiologyOrder> orders = radiologyOrderRepository.findAll().stream()
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> {
                    LocalDateTime dt = o.getOrderDate().atStartOfDay();
                    return !dt.isBefore(startDt) && dt.isBefore(endDt);
                })
                .collect(Collectors.toList());

        long totalOrders = orders.size();
        long scheduled = orders.stream().filter(o -> o.getStatus() == RadiologyOrderStatus.SCHEDULED).count();
        long completed = orders.stream().filter(o -> o.getStatus() == RadiologyOrderStatus.COMPLETED).count();
        long pending = orders.stream().filter(o -> o.getStatus() == RadiologyOrderStatus.ORDERED || o.getStatus() == RadiologyOrderStatus.SCHEDULED).count();
        long finalReports = orders.stream().filter(o -> o.getStatus() == RadiologyOrderStatus.COMPLETED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == RadiologyOrderStatus.CANCELLED).count();

        Map<String, Long> statusMap = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus() != null ? o.getStatus().name() : "UNKNOWN", Collectors.counting()));

        Map<String, Long> modalityMap = orders.stream()
                .filter(o -> o.getRadiologyTest() != null)
                .collect(Collectors.groupingBy(o -> o.getRadiologyTest().getModality() != null ? o.getRadiologyTest().getModality().name() : "GENERAL", Collectors.counting()));

        return RadiologyAnalyticsDto.builder()
                .totalOrders(totalOrders)
                .scheduledStudies(scheduled)
                .completedStudies(completed)
                .pendingReports(pending)
                .finalReports(finalReports)
                .cancelledOrders(cancelled)
                .radiologyRevenue(BigDecimal.valueOf(completed * 1200.0).setScale(2, RoundingMode.HALF_UP))
                .ordersByModality(modalityMap)
                .ordersByStatus(statusMap)
                .build();
    }

    // 10. Patient Analytics
    public PatientAnalyticsDto getPatientAnalytics(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        List<Patient> allPatients = patientRepository.findAll();
        long totalRegistered = allPatients.size();

        long newInPeriod = allPatients.stream()
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(startDt) && p.getCreatedAt().isBefore(endDt))
                .count();

        long returningCount = Math.max(0, totalRegistered - newInPeriod);

        Map<String, Long> genderMap = allPatients.stream()
                .collect(Collectors.groupingBy(p -> p.getGender() != null ? p.getGender() : "OTHER", Collectors.counting()));

        Map<String, Long> bloodGroupMap = allPatients.stream()
                .collect(Collectors.groupingBy(p -> p.getBloodGroup() != null ? p.getBloodGroup() : "UNKNOWN", Collectors.counting()));

        LocalDate today = LocalDate.now(IST_ZONE);

        Map<String, Long> ageGroupMap = new LinkedHashMap<>();
        ageGroupMap.put("0-12", 0L);
        ageGroupMap.put("13-18", 0L);
        ageGroupMap.put("19-35", 0L);
        ageGroupMap.put("36-50", 0L);
        ageGroupMap.put("51-65", 0L);
        ageGroupMap.put("66+", 0L);

        for (Patient p : allPatients) {
            if (p.getDateOfBirth() != null) {
                int age = Period.between(p.getDateOfBirth(), today).getYears();
                if (age <= 12) ageGroupMap.put("0-12", ageGroupMap.get("0-12") + 1);
                else if (age <= 18) ageGroupMap.put("13-18", ageGroupMap.get("13-18") + 1);
                else if (age <= 35) ageGroupMap.put("19-35", ageGroupMap.get("19-35") + 1);
                else if (age <= 50) ageGroupMap.put("36-50", ageGroupMap.get("36-50") + 1);
                else if (age <= 65) ageGroupMap.put("51-65", ageGroupMap.get("51-65") + 1);
                else ageGroupMap.put("66+", ageGroupMap.get("66+") + 1);
            }
        }

        return PatientAnalyticsDto.builder()
                .totalRegisteredPatients(totalRegistered)
                .newRegistrationsInPeriod(newInPeriod)
                .returningPatientsCount(returningCount)
                .genderDistribution(genderMap)
                .ageGroupDistribution(ageGroupMap)
                .bloodGroupDistribution(bloodGroupMap)
                .build();
    }

    // 11. Operational Doctor & Department Report
    public OperationalReportDto getOperationalReport(String period, String fromDate, String toDate) {
        LocalDateTime[] bounds = resolveDateRange(period, fromDate, toDate);
        LocalDateTime startDt = bounds[0];
        LocalDateTime endDt = bounds[1];

        List<Doctor> doctors = doctorRepository.findAll();
        List<Appointment> appts = appointmentRepository.findAll();
        List<OpdVisit> visits = opdVisitRepository.findAll();
        List<Prescription> prescriptions = prescriptionRepository.findAll();

        List<OperationalReportDto.DoctorPerformanceDto> docPerf = doctors.stream().map(d -> {
            long totalAppt = appts.stream()
                    .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(d.getId()))
                    .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().atStartOfDay().isBefore(startDt) && a.getAppointmentDate().atStartOfDay().isBefore(endDt))
                    .count();

            long completedAppt = appts.stream()
                    .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(d.getId()))
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().atStartOfDay().isBefore(startDt) && a.getAppointmentDate().atStartOfDay().isBefore(endDt))
                    .count();

            long opdCount = visits.stream()
                    .filter(v -> v.getDoctor() != null && v.getDoctor().getId().equals(d.getId()))
                    .filter(v -> v.getVisitDate() != null && !v.getVisitDate().atStartOfDay().isBefore(startDt) && v.getVisitDate().atStartOfDay().isBefore(endDt))
                    .count();

            long rxCount = prescriptions.stream()
                    .filter(p -> p.getDoctor() != null && p.getDoctor().getId().equals(d.getId()))
                    .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(startDt) && p.getCreatedAt().isBefore(endDt))
                    .count();

            return OperationalReportDto.DoctorPerformanceDto.builder()
                    .doctorId(d.getId())
                    .doctorName(d.getFirstName() + " " + d.getLastName())
                    .departmentName(d.getDepartment() != null ? d.getDepartment().getDepartmentName() : "GENERAL")
                    .specialization(d.getSpecialization())
                    .appointmentCount(totalAppt)
                    .completedAppointments(completedAppt)
                    .opdVisitsCount(opdCount)
                    .prescriptionsCount(rxCount)
                    .build();
        }).collect(Collectors.toList());

        List<Department> departments = departmentRepository.findAll();
        List<IpdAdmission> admissions = ipdAdmissionRepository.findAll();
        List<LabOrder> labOrders = labOrderRepository.findAll();
        List<RadiologyOrder> radOrders = radiologyOrderRepository.findAll();

        List<OperationalReportDto.DepartmentPerformanceDto> deptPerf = departments.stream().map(dept -> {
            long opdCount = visits.stream()
                    .filter(v -> v.getDepartment() != null && v.getDepartment().getId().equals(dept.getId()))
                    .filter(v -> v.getVisitDate() != null && !v.getVisitDate().atStartOfDay().isBefore(startDt) && v.getVisitDate().atStartOfDay().isBefore(endDt))
                    .count();

            long ipdCount = admissions.stream()
                    .filter(a -> a.getWard() != null && a.getWard().getDepartment() != null && a.getWard().getDepartment().getId().equals(dept.getId()))
                    .filter(a -> a.getAdmissionDate() != null && !a.getAdmissionDate().atStartOfDay().isBefore(startDt) && a.getAdmissionDate().atStartOfDay().isBefore(endDt))
                    .count();

            long labCount = labOrders.stream()
                    .filter(o -> o.getOrderDate() != null && !o.getOrderDate().atStartOfDay().isBefore(startDt) && o.getOrderDate().atStartOfDay().isBefore(endDt))
                    .count();

            long radCount = radOrders.stream()
                    .filter(o -> o.getOrderDate() != null && !o.getOrderDate().atStartOfDay().isBefore(startDt) && o.getOrderDate().atStartOfDay().isBefore(endDt))
                    .count();

            return OperationalReportDto.DepartmentPerformanceDto.builder()
                    .departmentId(dept.getId())
                    .departmentName(dept.getDepartmentName())
                    .departmentCode(dept.getDepartmentCode())
                    .opdVisitsCount(opdCount)
                    .ipdAdmissionsCount(ipdCount)
                    .labOrdersCount(labCount)
                    .radiologyOrdersCount(radCount)
                    .build();
        }).collect(Collectors.toList());

        return OperationalReportDto.builder()
                .doctorPerformances(docPerf)
                .departmentPerformances(deptPerf)
                .build();
    }

    // Export CSV string generator
    public String generateCSVReport(String reportType, String period, String fromDate, String toDate) {
        StringBuilder csv = new StringBuilder();

        if ("PAYMENTS".equalsIgnoreCase(reportType)) {
            PaymentCollectionReportDto report = getPaymentCollectionReport(period, fromDate, toDate);
            csv.append("Payment Method,Transaction Count,Total Collected (INR),Percentage of Total (%)\n");
            for (PaymentCollectionReportDto.MethodBreakdownDto m : report.getMethodBreakdown().values()) {
                csv.append(String.format("%s,%d,%.2f,%.2f\n", m.getPaymentMethod(), m.getCount(), m.getAmount().doubleValue(), m.getPercentageOfTotal()));
            }
            csv.append(String.format("TOTAL,%d,%.2f,100.00\n", report.getTotalTransactionsCount(), report.getTotalCollectedAmount().doubleValue()));
        } else if ("OUTSTANDING".equalsIgnoreCase(reportType)) {
            OutstandingReportDto report = getOutstandingReport();
            csv.append("Bill Number,Bill Date,Patient Code,Patient Name,Bill Type,Status,Total Amount (INR),Paid Amount (INR),Outstanding Amount (INR),Age (Days),Aging Bucket\n");
            for (OutstandingReportDto.OutstandingBillItemDto b : report.getOutstandingBills()) {
                csv.append(String.format("%s,%s,%s,\"%s\",%s,%s,%.2f,%.2f,%.2f,%d,%s\n",
                        b.getBillNumber(), b.getBillDate(), b.getPatientCode(), b.getPatientName(),
                        b.getBillType(), b.getStatus(), b.getTotalAmount().doubleValue(),
                        b.getPaidAmount().doubleValue(), b.getOutstandingAmount().doubleValue(),
                        b.getAgeDays(), b.getAgingBucket()));
            }
        } else if ("BED_OCCUPANCY".equalsIgnoreCase(reportType)) {
            BedOccupancyReportDto report = getBedOccupancyReport();
            csv.append("Ward Name,Ward Type,Total Beds,Available Beds,Occupied Beds,Cleaning Beds,Occupancy (%)\n");
            for (BedOccupancyReportDto.WardOccupancyDto w : report.getWardOccupancies()) {
                csv.append(String.format("\"%s\",%s,%d,%d,%d,%d,%.2f\n",
                        w.getWardName(), w.getWardType(), w.getTotalBeds(), w.getAvailableBeds(),
                        w.getOccupiedBeds(), w.getCleaningBeds(), w.getOccupancyPercentage()));
            }
        } else { // Default OPERATIONAL
            OperationalReportDto report = getOperationalReport(period, fromDate, toDate);
            csv.append("Doctor Name,Department,Specialization,Total Appointments,Completed Appointments,OPD Visits,Prescriptions Issued\n");
            for (OperationalReportDto.DoctorPerformanceDto d : report.getDoctorPerformances()) {
                csv.append(String.format("\"%s\",\"%s\",\"%s\",%d,%d,%d,%d\n",
                        d.getDoctorName(), d.getDepartmentName(), d.getSpecialization(),
                        d.getAppointmentCount(), d.getCompletedAppointments(),
                        d.getOpdVisitsCount(), d.getPrescriptionsCount()));
            }
        }

        return csv.toString();
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
