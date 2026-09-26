package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentRequestDto {

    @NotBlank(message = "Department code is required")
    @Pattern(regexp = "^[A-Z0-9_-]{2,20}$", message = "Department code must be 2-20 uppercase alphanumeric characters")
    private String departmentCode;

    @NotBlank(message = "Department name is required")
    private String departmentName;

    private String description;
    private String location;
    private Boolean isActive;
}
