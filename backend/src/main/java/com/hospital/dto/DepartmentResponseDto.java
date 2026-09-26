package com.hospital.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponseDto {
    private Long id;
    private String departmentCode;
    private String departmentName;
    private String description;
    private String location;
    private Boolean isActive;
    private long doctorCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
