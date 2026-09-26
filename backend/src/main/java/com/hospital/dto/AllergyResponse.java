package com.hospital.dto;

import com.hospital.entity.AllergySeverity;
import com.hospital.entity.AllergyStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private String allergen;
    private String allergyType;
    private String reaction;
    private AllergySeverity severity;
    private AllergyStatus status;
    private String recordedByName;
    private LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
}
