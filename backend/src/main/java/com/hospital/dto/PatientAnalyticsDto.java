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
public class PatientAnalyticsDto {
    private long totalRegisteredPatients;
    private long newRegistrationsInPeriod;
    private long returningPatientsCount;
    
    private Map<String, Long> genderDistribution;
    private Map<String, Long> ageGroupDistribution; // 0-12, 13-18, 19-35, 36-50, 51-65, 66+
    private Map<String, Long> bloodGroupDistribution;
}
