package com.hospital.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InpatientProgressNoteResponse {
    private Long id;
    private String progressNoteId;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime noteDatetime;
    private String clinicalAssessment;
    private String progressSummary;
    private String diagnosisUpdate;
    private String treatmentUpdate;
    private String followUpPlan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
