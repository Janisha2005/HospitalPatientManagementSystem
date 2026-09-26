package com.hospital.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDto {
    private String period; // TODAY, THIS_WEEK, THIS_MONTH, YEAR_TO_DATE
    private BigDecimal totalBilled;
    private BigDecimal totalCollected;
    private BigDecimal totalOutstanding;
    private BigDecimal totalRefunded;
    private Map<String, BigDecimal> revenueByCategory;
    private Map<String, BigDecimal> collectionByPaymentMethod;
}
