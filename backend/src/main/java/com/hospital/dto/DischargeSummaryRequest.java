package com.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargeSummaryRequest {
    private String admissionSummary;
    private String clinicalCourse;
    private String finalDiagnosis;
    private String proceduresSummary;
    private String investigationSummary;
    private String treatmentSummary;
    private String medicationSummary;
    private String conditionAtDischarge;
    private String followUpInstructions;

    @NotNull(message = "Discharge date is required")
    private LocalDate dischargeDate;
}
