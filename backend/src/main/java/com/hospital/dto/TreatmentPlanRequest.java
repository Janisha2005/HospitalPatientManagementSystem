package com.hospital.dto;

import com.hospital.entity.TreatmentPlanStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlanRequest {
    private Long patientId;
    private Long doctorId;
    @NotBlank(message = "Plan details are required")
    private String planDetails;
    private LocalDate followUpDate;
    @NotNull(message = "Treatment plan status is required")
    private TreatmentPlanStatus status;
}
