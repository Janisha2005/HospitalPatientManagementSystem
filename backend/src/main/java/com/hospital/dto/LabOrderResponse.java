package com.hospital.dto;

import com.hospital.entity.LabOrderStatus;
import com.hospital.entity.OrderPriority;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderResponse {
    private Long id;
    private String labOrderId;
    private Long patientId;
    private String patientCode;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long opdVisitId;
    private String opdVisitCode;
    private Long departmentId;
    private String departmentName;
    private LocalDate orderDate;
    private OrderPriority priority;
    private LabOrderStatus status;
    private String clinicalNote;
    private LocalDateTime sampleCollectedAt;
    private String sampleCollectedByName;
    private List<LabOrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
