package com.hospital.service;

import com.hospital.dto.BillDto;
import com.hospital.dto.BillingDashboardDto;
import com.hospital.dto.PaymentDto;
import com.hospital.dto.RevenueReportDto;
import com.hospital.entity.BillType;
import com.hospital.entity.PaymentMethod;
import com.hospital.repository.BillRepository;
import com.hospital.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingDashboardService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final BillingService billingService;
    private final PaymentService paymentService;

    public static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    @Transactional(readOnly = true)
    public BillingDashboardDto getDashboardMetrics() {
        LocalDate today = LocalDate.now(INDIA_ZONE);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<com.hospital.entity.Bill> todayBills = billRepository.findByBillDateBetween(today, today);
        long todayCount = todayBills.size();

        BigDecimal todayBilled = billRepository.getDailyBilledAmount(today);
        BigDecimal todayCollections = paymentRepository.getTotalPaymentsBetween(startOfDay, endOfDay);
        BigDecimal totalOutstanding = billRepository.getTotalOutstandingAmount();

        BigDecimal opdRev = billRepository.getTotalRevenueByBillType(BillType.OPD);
        BigDecimal ipdRev = billRepository.getTotalRevenueByBillType(BillType.IPD);
        BigDecimal pharmRev = billRepository.getTotalRevenueByBillType(BillType.PHARMACY);
        BigDecimal labRev = billRepository.getTotalRevenueByBillType(BillType.LABORATORY);
        BigDecimal radRev = billRepository.getTotalRevenueByBillType(BillType.RADIOLOGY);

        List<BillDto> recentBills = billRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                .stream().map(billingService::mapToDto).collect(Collectors.toList());

        List<PaymentDto> recentPayments = paymentRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                .stream().map(paymentService::mapToDto).collect(Collectors.toList());

        return BillingDashboardDto.builder()
                .todayBillsCount(todayCount)
                .todayBilledAmount(todayBilled)
                .todayCollectionsAmount(todayCollections)
                .totalOutstandingAmount(totalOutstanding)
                .opdRevenue(opdRev)
                .ipdRevenue(ipdRev)
                .pharmacyRevenue(pharmRev)
                .laboratoryRevenue(labRev)
                .radiologyRevenue(radRev)
                .recentBills(recentBills)
                .recentPayments(recentPayments)
                .build();
    }

    @Transactional(readOnly = true)
    public RevenueReportDto getRevenueReport(String period) {
        LocalDate today = LocalDate.now(INDIA_ZONE);
        LocalDate startDate;

        if ("THIS_WEEK".equalsIgnoreCase(period)) {
            startDate = today.minusDays(7);
        } else if ("THIS_MONTH".equalsIgnoreCase(period)) {
            startDate = today.withDayOfMonth(1);
        } else if ("YEAR_TO_DATE".equalsIgnoreCase(period)) {
            startDate = today.withDayOfYear(1);
        } else {
            startDate = today; // TODAY
        }

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = today.atTime(LocalTime.MAX);

        BigDecimal totalBilled = billRepository.findByBillDateBetween(startDate, today)
                .stream().map(b -> b.getGrandTotal() != null ? b.getGrandTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = paymentRepository.getTotalPaymentsBetween(startDt, endDt);
        BigDecimal totalOutstanding = billRepository.getTotalOutstandingAmount();

        Map<String, BigDecimal> revByCategory = new HashMap<>();
        for (BillType type : BillType.values()) {
            revByCategory.put(type.name(), billRepository.getTotalRevenueByBillType(type));
        }

        Map<String, BigDecimal> colByMethod = new HashMap<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            colByMethod.put(method.name(), paymentRepository.getTotalPaymentsByMethod(method));
        }

        return RevenueReportDto.builder()
                .period(period)
                .totalBilled(totalBilled)
                .totalCollected(totalCollected)
                .totalOutstanding(totalOutstanding)
                .totalRefunded(BigDecimal.ZERO)
                .revenueByCategory(revByCategory)
                .collectionByPaymentMethod(colByMethod)
                .build();
    }
}
