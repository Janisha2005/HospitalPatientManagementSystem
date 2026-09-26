package com.hospital.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InpatientVitalResponse {
    private Long id;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private String recordedBy;
    private LocalDateTime recordedAt;
    private BigDecimal temperature;
    private Integer pulse;
    private String bloodPressure;
    private Integer respiratoryRate;
    private Integer oxygenSaturation;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private Integer painScore;
    private String notes;
    private LocalDateTime createdAt;
}
