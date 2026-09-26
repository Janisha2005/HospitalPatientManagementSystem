package com.hospital.dto;

import com.hospital.entity.PurchaseOrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class PurchaseOrderDto {
    private Long id;
    private String purchaseOrderId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;
    private String supplierName;

    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private PurchaseOrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal grandTotal;
    private String notes;
    private String createdBy;

    @NotEmpty(message = "Purchase order must contain at least one item")
    private List<PurchaseOrderItemDto> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
