package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OPDAnalyticsDto {
    private long totalVisits;
    private long uniquePatients;
    private long newPatients;
    private long returningPatients;
    private double averageVisitsPerDay;
    private double noShowRate;
    
    private Map<String, Long> visitsByDepartment;
    private Map<String, Long> visitsByDoctor;
    private Map<String, Long> visitsByStatus;
    private Map<String, Long> visitsByAppointmentType;
    
    private List<DailyTrendDto> dailyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendDto {
        private String date; // YYYY-MM-DD
        private long count;
        private double revenue;
    }
}
