package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedOccupancyReportDto {
    private long totalBeds;
    private long availableBeds;
    private long occupiedBeds;
    private long cleaningBeds;
    private long maintenanceBeds;
    private double overallOccupancyPercentage;
    
    private List<WardOccupancyDto> wardOccupancies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WardOccupancyDto {
        private Long wardId;
        private String wardName;
        private String wardType;
        private long totalBeds;
        private long availableBeds;
        private long occupiedBeds;
        private long cleaningBeds;
        private double occupancyPercentage;
        private double dailyRate;
    }
}
