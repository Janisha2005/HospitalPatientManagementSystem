package com.hospital.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineResponse {
    private Long id;
    private String medicineCode;
    private String medicineName;
    private String genericName;
    private String strength;
    private String dosageForm;
    private String manufacturer;
    private MedicineCategoryDto category;
    private String unit;
    private Integer reorderLevel;
    private Integer maximumStockLevel;
    private Boolean isPrescriptionRequired;
    private Boolean isControlled;
    private Integer availableStock;
    private Boolean isLowStock;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
