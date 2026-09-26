package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryAnalyticsDto {
    private long totalOrders;
    private long samplesCollected;
    private long testsCompleted;
    private long pendingTests;
    private long verifiedResults;
    private long cancelledOrders;
    private BigDecimal labRevenue;
    
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> ordersByPriority;
}
