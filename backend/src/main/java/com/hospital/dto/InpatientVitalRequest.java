package com.hospital.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InpatientVitalRequest {
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
}
