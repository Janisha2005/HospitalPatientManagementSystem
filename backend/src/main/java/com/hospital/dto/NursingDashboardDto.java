package com.hospital.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NursingDashboardDto {
    private long currentInpatientsCount;
    private long newAdmissionsToday;
    private long pendingTransfersCount;
    private long patientsDueForVitalsCount;
    private long criticalPatientsCount;
    private long dischargePlannedCount;
    private long availableBedsCount;
    private List<IpdAdmissionResponse> activeInpatients;
}
