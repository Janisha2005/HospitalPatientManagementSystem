package com.hospital.dto;

import com.hospital.entity.DispensingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyDispensingDto {
    private Long id;
    private String dispensingId;
    private Long prescriptionId;
    private Long prescriptionItemId;
    private Long patientId;
    private String patientName;
    private String patientIdCode;
    private Long doctorId;
    private String doctorName;
    private Long medicineId;
    private String medicineName;
    private Long batchId;
    private String batchNumber;
    private Long ipdAdmissionId;
    private Integer prescribedQuantity;
    private Integer dispensedQuantity;
    private Integer remainingQuantity;
    private String dispensedBy;
    private LocalDateTime dispensedDatetime;
    private DispensingStatus status;
    private String remarks;
    private LocalDateTime createdAt;
}
