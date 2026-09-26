package com.hospital.dto;

import com.hospital.entity.RadiologyModality;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyTestResponse {
    private Long id;
    private String testCode;
    private String testName;
    private RadiologyModality modality;
    private String bodyPart;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
