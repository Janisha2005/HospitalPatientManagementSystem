package com.hospital.dto;

import com.hospital.entity.OrderPriority;
import com.hospital.entity.RadiologyModality;
import com.hospital.entity.RadiologyOrderStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyOrderResponse {
    private Long id;
    private String radiologyOrderId;
    private Long patientId;
    private String patientCode;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long opdVisitId;
    private String opdVisitCode;
    private Long departmentId;
    private String departmentName;
    private Long radiologyTestId;
    private String testCode;
    private String testName;
    private RadiologyModality modality;
    private LocalDate orderDate;
    private OrderPriority priority;
    private RadiologyOrderStatus status;
    private String clinicalIndication;
    private RadiologyReportResponse report;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
