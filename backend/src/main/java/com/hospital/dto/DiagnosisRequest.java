package com.hospital.dto;

import com.hospital.entity.DiagnosisStatus;
import com.hospital.entity.DiagnosisType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisRequest {
    private Long patientId;
    private Long doctorId;
    @NotBlank(message = "Diagnosis name is required")
    private String diagnosisName;
    private String diagnosisDescription;
    @NotNull(message = "Diagnosis type is required")
    private DiagnosisType diagnosisType;
    @NotNull(message = "Diagnosis status is required")
    private DiagnosisStatus status;
}
