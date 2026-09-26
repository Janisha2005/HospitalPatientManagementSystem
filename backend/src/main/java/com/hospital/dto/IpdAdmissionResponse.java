package com.hospital.dto;

import com.hospital.entity.AdmissionStatus;
import com.hospital.entity.AdmissionType;
import com.hospital.entity.DischargeType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpdAdmissionResponse {
    private Long id;
    private String admissionId;
    private Long patientId;
    private String patientName;
    private String patientCode;
    private String patientPhone;
    private String patientGender;
    private Integer patientAge;
    private Long admittingDoctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private Long wardId;
    private String wardCode;
    private String wardName;
    private Long bedId;
    private String bedCode;
    private String bedNumber;
    private Long opdVisitId;
    private String opdVisitNumber;
    private AdmissionType admissionType;
    private LocalDate admissionDate;
    private LocalTime admissionTime;
    private String reasonForAdmission;
    private String clinicalSummary;
    private AdmissionStatus status;
    private LocalDate expectedDischargeDate;
    private LocalDate actualDischargeDate;
    private DischargeType dischargeType;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
