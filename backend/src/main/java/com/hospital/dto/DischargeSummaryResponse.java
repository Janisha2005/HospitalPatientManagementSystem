package com.hospital.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargeSummaryResponse {
    private Long id;
    private String dischargeSummaryId;
    private Long admissionId;
    private String admissionCode;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String admissionSummary;
    private String clinicalCourse;
    private String finalDiagnosis;
    private String proceduresSummary;
    private String investigationSummary;
    private String treatmentSummary;
    private String medicationSummary;
    private String conditionAtDischarge;
    private String followUpInstructions;
    private LocalDate dischargeDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
