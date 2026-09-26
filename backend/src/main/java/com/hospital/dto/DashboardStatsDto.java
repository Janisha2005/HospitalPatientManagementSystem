package com.hospital.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalPatients;
    private long totalDoctors;
    private long totalDepartments;
    private long activeUsers;
}
