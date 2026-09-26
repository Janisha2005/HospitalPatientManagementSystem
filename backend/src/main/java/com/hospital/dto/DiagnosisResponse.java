package com.hospital.dto;

import com.hospital.entity.DiagnosisStatus;
import com.hospital.entity.DiagnosisType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisResponse {
    private Long id;
    private String diagnosisId;
    private Long patientId;
    private String patientName;
    private Long opdVisitId;
    private String opdVisitCode;
    private Long doctorId;
    private String doctorName;
    private String diagnosisName;
    private String diagnosisDescription;
    private DiagnosisType diagnosisType;
    private DiagnosisStatus status;
    private LocalDateTime diagnosedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
