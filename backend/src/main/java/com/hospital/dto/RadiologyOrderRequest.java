package com.hospital.dto;

import com.hospital.entity.OrderPriority;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyOrderRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    private Long opdVisitId;
    @NotNull(message = "Department ID is required")
    private Long departmentId;
    @NotNull(message = "Radiology test ID is required")
    private Long radiologyTestId;
    @NotNull(message = "Order priority is required")
    private OrderPriority priority;
    private String clinicalIndication;
}
