package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPDAnalyticsDto {
    private long totalAdmissions;
    private long currentAdmissions;
    private long totalDischarges;
    private double averageLengthOfStayDays;
    private double bedOccupancyPercentage;
    
    private long totalBeds;
    private long availableBeds;
    private long occupiedBeds;
    private long cleaningBeds;
    
    private Map<String, Long> admissionsByWard;
    private Map<String, Long> dischargesByWard;
    private Map<String, Long> admissionsByStatus;
    private Map<String, Long> clearanceStatusBreakdown;
    private long totalTransfersCount;
}
