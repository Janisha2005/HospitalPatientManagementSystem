package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutstandingReportDto {
    private BigDecimal totalOutstandingAmount;
    private long totalUnpaidBillsCount;
    
    // Aging Buckets
    private BigDecimal currentBucketAmount;     // 0-30 days
    private BigDecimal bucket31To60Amount;      // 31-60 days
    private BigDecimal bucket61To90Amount;      // 61-90 days
    private BigDecimal bucketOver90Amount;      // 90+ days

    private List<OutstandingBillItemDto> outstandingBills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutstandingBillItemDto {
        private Long billId;
        private String billNumber;
        private String billDate;
        private Long patientId;
        private String patientName;
        private String patientCode;
        private String billType;
        private String status;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal outstandingAmount;
        private long ageDays;
        private String agingBucket;
    }
}
