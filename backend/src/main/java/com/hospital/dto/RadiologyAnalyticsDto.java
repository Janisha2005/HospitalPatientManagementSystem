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
public class RadiologyAnalyticsDto {
    private long totalOrders;
    private long scheduledStudies;
    private long completedStudies;
    private long pendingReports;
    private long finalReports;
    private long cancelledOrders;
    private BigDecimal radiologyRevenue;
    
    private Map<String, Long> ordersByModality;
    private Map<String, Long> ordersByStatus;
}
