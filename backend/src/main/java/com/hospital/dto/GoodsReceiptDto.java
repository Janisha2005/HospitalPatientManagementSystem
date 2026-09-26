package com.hospital.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptDto {
    private Long id;
    private String goodsReceiptId;
    private Long purchaseOrderId;
    private String purchaseOrderNumber;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;
    private String supplierName;

    private LocalDate receiptDate;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String receivedBy;
    private String remarks;

    @NotEmpty(message = "Goods receipt must contain items")
    private List<GoodsReceiptItemDto> items;

    private LocalDateTime createdAt;
}
