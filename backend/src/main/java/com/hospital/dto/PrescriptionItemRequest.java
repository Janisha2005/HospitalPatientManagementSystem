package com.hospital.dto;

import com.hospital.entity.PrescriptionFrequency;
import com.hospital.entity.PrescriptionRoute;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItemRequest {
    @NotBlank(message = "Medicine name is required")
    private String medicineName;
    private String strength;
    private String dosage;
    private PrescriptionRoute route;
    private PrescriptionFrequency frequency;
    private Integer durationValue;
    private String durationUnit;
    private String quantity;
    private String instructions;
}
