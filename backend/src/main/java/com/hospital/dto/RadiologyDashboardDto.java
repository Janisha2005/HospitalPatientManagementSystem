package com.hospital.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyDashboardDto {
    private long todaysOrders;
    private long scheduled;
    private long pendingPerformance;
    private long reportsDraft;
    private long reportsPendingVerification;
    private long verifiedReports;
}
