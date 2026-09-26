package com.hospital.dto;

import com.hospital.entity.OrderPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    private Long opdVisitId;
    @NotNull(message = "Department ID is required")
    private Long departmentId;
    @NotNull(message = "Order priority is required")
    private OrderPriority priority;
    private String clinicalNote;
    @NotEmpty(message = "Lab order must contain at least one test item")
    @Valid
    private List<LabOrderItemRequest> items;
}
