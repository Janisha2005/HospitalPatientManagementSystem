package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpdDashboardDto {

    private long todaysAppointments;
    private long checkedInPatients;
    private long waitingPatients;
    private long inConsultationPatients;
    private long completedVisits;
    private long cancelledAppointments;
    private long noShowAppointments;

    // Doctor specific counts if requested
    private Long doctorId;
    private long myAppointmentsToday;
    private long myWaitingPatients;
    private long myInConsultationPatients;
    private long myCompletedVisits;
}
