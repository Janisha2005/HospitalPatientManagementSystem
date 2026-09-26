package com.hospital.dto;

import com.hospital.entity.ReturnType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyReturnDto {
    private Long id;
    private String returnId;
    private ReturnType returnType;
    private Long patientId;
    private String patientName;
    private Long supplierId;
    private String supplierName;
    private Long dispensingId;
    private LocalDate returnDate;
    private String reason;
    private BigDecimal refundAmount;
    private String processedBy;
    private String remarks;
    private List<PharmacyReturnItemDto> items;
    private LocalDateTime createdAt;
}
