package com.hospital.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpdDashboardDto {
    private long totalActiveAdmissions;
    private long todayAdmissions;
    private long todayDischarges;
    private long availableBeds;
    private long occupiedBeds;
    private long bedsInCleaning;
    private long bedsUnderMaintenance;
    private long pendingAdmissionRequests;
    private long dischargePlanned;
}
