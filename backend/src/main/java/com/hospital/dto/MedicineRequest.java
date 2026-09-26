package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineRequest {
    @NotBlank(message = "Medicine code is required")
    private String medicineCode;

    @NotBlank(message = "Medicine name is required")
    private String medicineName;

    private String genericName;
    private String strength;
    private String dosageForm;
    private String manufacturer;
    private Long categoryId;
    private String unit;
    private Integer reorderLevel;
    private Integer maximumStockLevel;
    private Boolean isPrescriptionRequired;
    private Boolean isControlled;
    private Boolean isActive;
}
