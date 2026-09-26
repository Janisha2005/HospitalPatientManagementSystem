package com.hospital.dto;

import com.hospital.entity.OpdVisitStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpdVisitUpdateRequest {

    private String chiefComplaint;
    private String clinicalNotes;
    private String diagnosis;
    private String treatmentPlan;

    @DecimalMin(value = "25.0", message = "Temperature must be at least 25.0 °C")
    @DecimalMax(value = "45.0", message = "Temperature must not exceed 45.0 °C")
    private BigDecimal vitalTemperature;

    @Min(value = 20, message = "Pulse must be at least 20 bpm")
    @Max(value = 250, message = "Pulse must not exceed 250 bpm")
    private Integer vitalPulse;

    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "Blood pressure must be in format SYS/DIA (e.g. 120/80)")
    private String vitalBloodPressure;

    @Min(value = 5, message = "Respiratory rate must be at least 5 breaths/min")
    @Max(value = 80, message = "Respiratory rate must not exceed 80 breaths/min")
    private Integer vitalRespiratoryRate;

    @Min(value = 50, message = "Oxygen saturation must be at least 50%")
    @Max(value = 100, message = "Oxygen saturation must not exceed 100%")
    private Integer vitalOxygenSaturation;

    @DecimalMin(value = "30.0", message = "Height must be at least 30 cm")
    @DecimalMax(value = "250.0", message = "Height must not exceed 250 cm")
    private BigDecimal heightCm;

    @DecimalMin(value = "1.0", message = "Weight must be at least 1 kg")
    @DecimalMax(value = "500.0", message = "Weight must not exceed 500 kg")
    private BigDecimal weightKg;

    private OpdVisitStatus visitStatus;
}
