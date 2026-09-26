package com.hospital.dto;

import com.hospital.entity.OpdVisitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpdVisitResponse {

    private Long id;
    private String opdVisitId;

    private Long appointmentId;
    private String appointmentCode;

    private Long patientId;
    private String patientCode;
    private String patientName;
    private String patientPhone;
    private Integer patientAge;
    private String patientGender;

    private Long doctorId;
    private String doctorCode;
    private String doctorName;
    private String doctorSpecialization;

    private Long departmentId;
    private String departmentName;

    private LocalDate visitDate;
    private Integer queueNumber;

    private String chiefComplaint;
    private String clinicalNotes;
    private String diagnosis;
    private String treatmentPlan;

    private BigDecimal vitalTemperature;
    private Integer vitalPulse;
    private String vitalBloodPressure;
    private Integer vitalRespiratoryRate;
    private Integer vitalOxygenSaturation;
    private BigDecimal heightCm;
    private BigDecimal weightKg;

    private OpdVisitStatus visitStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
