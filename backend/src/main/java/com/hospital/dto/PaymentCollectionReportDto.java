package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCollectionReportDto {
    private BigDecimal totalCollectedAmount;
    private long totalTransactionsCount;
    
    private Map<String, MethodBreakdownDto> methodBreakdown;
    private List<DailyCollectionDto> dailyCollectionTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodBreakdownDto {
        private String paymentMethod;
        private BigDecimal amount;
        private long count;
        private double percentageOfTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCollectionDto {
        private String date; // YYYY-MM-DD
        private BigDecimal totalAmount;
        private long transactionCount;
        private Map<String, BigDecimal> methodAmounts;
    }
}
