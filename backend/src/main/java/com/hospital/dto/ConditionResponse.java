package com.hospital.dto;

import com.hospital.entity.ConditionStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private String conditionName;
    private String description;
    private LocalDate diagnosedDate;
    private ConditionStatus status;
    private String recordedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
