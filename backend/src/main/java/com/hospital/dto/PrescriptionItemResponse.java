package com.hospital.dto;

import com.hospital.entity.PrescriptionFrequency;
import com.hospital.entity.PrescriptionRoute;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItemResponse {
    private Long id;
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
