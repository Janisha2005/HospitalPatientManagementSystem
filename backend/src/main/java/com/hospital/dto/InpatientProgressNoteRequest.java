package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InpatientProgressNoteRequest {
    private LocalDateTime noteDatetime;
    private String clinicalAssessment;

    @NotBlank(message = "Progress summary is required")
    private String progressSummary;

    private String diagnosisUpdate;
    private String treatmentUpdate;
    private String followUpPlan;
}
