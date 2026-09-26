package com.hospital.dto;

import com.hospital.entity.ChargeCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeMasterDto {
    private Long id;
    private String chargeCode;

    @NotBlank(message = "Charge name is required")
    private String chargeName;

    @NotNull(message = "Charge category is required")
    private ChargeCategory chargeCategory;

    private String description;
    private Long departmentId;
    private String departmentName;
    private String unit;

    @NotNull(message = "Base rate is required")
    @DecimalMin(value = "0.0", message = "Base rate cannot be negative")
    private BigDecimal baseRate;

    private BigDecimal taxPercentage;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
