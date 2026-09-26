package com.hospital.dto;

import com.hospital.entity.AdmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpdAdmissionCreateRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Admitting Doctor ID is required")
    private Long admittingDoctorId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long wardId;
    private Long bedId;
    private Long opdVisitId;

    @NotNull(message = "Admission type is required")
    private AdmissionType admissionType;

    @NotNull(message = "Admission date is required")
    private LocalDate admissionDate;

    private LocalTime admissionTime;

    @NotBlank(message = "Reason for admission is required")
    private String reasonForAdmission;

    private String clinicalSummary;
    private LocalDate expectedDischargeDate;
}
