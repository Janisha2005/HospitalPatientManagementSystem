package com.hospital.dto;

import com.hospital.entity.WardGenderPolicy;
import com.hospital.entity.WardType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardResponse {
    private Long id;
    private String wardCode;
    private String wardName;
    private WardType wardType;
    private Long departmentId;
    private String departmentName;
    private String floor;
    private String building;
    private WardGenderPolicy genderPolicy;
    private Integer capacity;
    private Integer occupiedBeds;
    private Integer availableBeds;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
