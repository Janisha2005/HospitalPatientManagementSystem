package com.hospital.dto;

import com.hospital.entity.WardGenderPolicy;
import com.hospital.entity.WardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardRequest {

    @NotBlank(message = "Ward code is required")
    private String wardCode;

    @NotBlank(message = "Ward name is required")
    private String wardName;

    @NotNull(message = "Ward type is required")
    private WardType wardType;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private String floor;
    private String building;

    @NotNull(message = "Gender policy is required")
    private WardGenderPolicy genderPolicy;

    private Integer capacity;
    private Boolean isActive;
}
