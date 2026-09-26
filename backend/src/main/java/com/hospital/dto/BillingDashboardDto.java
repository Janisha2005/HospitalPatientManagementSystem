package com.hospital.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingDashboardDto {
    private long todayBillsCount;
    private BigDecimal todayBilledAmount;
    private BigDecimal todayCollectionsAmount;
    private BigDecimal totalOutstandingAmount;

    private BigDecimal opdRevenue;
    private BigDecimal ipdRevenue;
    private BigDecimal pharmacyRevenue;
    private BigDecimal laboratoryRevenue;
    private BigDecimal radiologyRevenue;

    private List<BillDto> recentBills;
    private List<PaymentDto> recentPayments;
}
