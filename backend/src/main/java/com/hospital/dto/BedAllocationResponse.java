package com.hospital.dto;

import com.hospital.entity.AllocationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedAllocationResponse {
    private Long id;
    private Long admissionId;
    private String admissionCode;
    private Long patientId;
    private String patientName;
    private Long wardId;
    private String wardCode;
    private String wardName;
    private Long bedId;
    private String bedCode;
    private String bedNumber;
    private AllocationType allocationType;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String allocatedBy;
    private String reason;
    private LocalDateTime createdAt;
}
