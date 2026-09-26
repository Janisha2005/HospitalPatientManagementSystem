package com.hospital.dto;

import com.hospital.entity.TreatmentPlanStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlanResponse {
    private Long id;
    private String treatmentPlanId;
    private Long patientId;
    private String patientName;
    private Long opdVisitId;
    private String opdVisitCode;
    private Long doctorId;
    private String doctorName;
    private String planDetails;
    private LocalDate followUpDate;
    private TreatmentPlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
