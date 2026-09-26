package com.hospital.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargePlanResponse {
    private Long id;
    private String dischargePlanId;
    private Long admissionId;
    private String admissionCode;
    private Long patientId;
    private String patientName;
    private LocalDate plannedDischargeDate;
    private String dischargeCondition;
    private Boolean followUpRequired;
    private LocalDate followUpDate;
    private String followUpInstructions;
    private String homeCareInstructions;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
