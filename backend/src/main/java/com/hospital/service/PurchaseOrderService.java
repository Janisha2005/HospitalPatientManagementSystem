package com.hospital.service;

import com.hospital.dto.PurchaseOrderDto;
import com.hospital.dto.PurchaseOrderItemDto;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.MedicineRepository;
import com.hospital.repository.PurchaseOrderItemRepository;
import com.hospital.repository.PurchaseOrderRepository;
import com.hospital.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<PurchaseOrderDto> getAllPurchaseOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDto getPurchaseOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        return mapToDto(po);
    }

    @Transactional
    public PurchaseOrderDto createPurchaseOrder(PurchaseOrderDto dto, String username) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + dto.getSupplierId()));

        String poId = generatePurchaseOrderId();

        PurchaseOrder po = PurchaseOrder.builder()
                .purchaseOrderId(poId)
                .supplier(supplier)
                .orderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now())
                .expectedDeliveryDate(dto.getExpectedDeliveryDate())
                .status(PurchaseOrderStatus.DRAFT)
                .notes(dto.getNotes())
                .createdBy(username)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        List<PurchaseOrderItem> items = new ArrayList<>();

        for (PurchaseOrderItemDto itemDto : dto.getItems()) {
            Medicine medicine = medicineRepository.findById(itemDto.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + itemDto.getMedicineId()));

            int qty = itemDto.getOrderedQuantity();
            BigDecimal unitCost = itemDto.getUnitCost();
            BigDecimal taxPct = itemDto.getTaxPercentage() != null ? itemDto.getTaxPercentage() : BigDecimal.ZERO;
            BigDecimal discount = itemDto.getDiscountAmount() != null ? itemDto.getDiscountAmount() : BigDecimal.ZERO;

            BigDecimal baseLine = unitCost.multiply(BigDecimal.valueOf(qty));
            BigDecimal taxAmt = baseLine.multiply(taxPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = baseLine.add(taxAmt).subtract(discount);

            subtotal = subtotal.add(baseLine);
            totalTax = totalTax.add(taxAmt);
            totalDiscount = totalDiscount.add(discount);

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .medicine(medicine)
                    .orderedQuantity(qty)
                    .receivedQuantity(0)
                    .unitCost(unitCost)
                    .taxPercentage(taxPct)
                    .discountAmount(discount)
                    .lineTotal(lineTotal)
                    .build();

            items.add(item);
        }

        BigDecimal grandTotal = subtotal.add(totalTax).subtract(totalDiscount);
        po.setSubtotal(subtotal);
        po.setTaxAmount(totalTax);
        po.setDiscountAmount(totalDiscount);
        po.setGrandTotal(grandTotal);

        PurchaseOrder savedPo = purchaseOrderRepository.save(po);
        purchaseOrderItemRepository.saveAll(items);

        auditLogService.logAction("CREATE_PURCHASE_ORDER", "PurchaseOrder", savedPo.getId().toString(),
                "Created Purchase Order: " + savedPo.getPurchaseOrderId() + " for Supplier: " + supplier.getSupplierName());

        return mapToDto(savedPo);
    }

    @Transactional
    public PurchaseOrderDto updateStatus(Long id, PurchaseOrderStatus newStatus) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED && newStatus != PurchaseOrderStatus.RECEIVED) {
            throw new BadRequestException("Cannot change status of a fully received Purchase Order");
        }

        po.setStatus(newStatus);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        auditLogService.logAction("UPDATE_PURCHASE_ORDER_STATUS", "PurchaseOrder", saved.getId().toString(),
                "Updated status to " + newStatus + " for PO: " + saved.getPurchaseOrderId());

        return mapToDto(saved);
    }

    private String generatePurchaseOrderId() {
        long count = purchaseOrderRepository.count() + 1;
        return String.format("PO-%d-%06d", LocalDate.now().getYear(), count);
    }

    public PurchaseOrderDto mapToDto(PurchaseOrder po) {
        if (po == null) return null;

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(po.getId());
        List<PurchaseOrderItemDto> itemDtos = items.stream().map(i -> PurchaseOrderItemDto.builder()
                .id(i.getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getMedicineName())
                .medicineCode(i.getMedicine().getMedicineCode())
                .orderedQuantity(i.getOrderedQuantity())
                .receivedQuantity(i.getReceivedQuantity())
                .unitCost(i.getUnitCost())
                .taxPercentage(i.getTaxPercentage())
                .discountAmount(i.getDiscountAmount())
                .lineTotal(i.getLineTotal())
                .build()).collect(Collectors.toList());

        return PurchaseOrderDto.builder()
                .id(po.getId())
                .purchaseOrderId(po.getPurchaseOrderId())
                .supplierId(po.getSupplier().getId())
                .supplierName(po.getSupplier().getSupplierName())
                .orderDate(po.getOrderDate())
                .expectedDeliveryDate(po.getExpectedDeliveryDate())
                .status(po.getStatus())
                .subtotal(po.getSubtotal())
                .taxAmount(po.getTaxAmount())
                .discountAmount(po.getDiscountAmount())
                .grandTotal(po.getGrandTotal())
                .notes(po.getNotes())
                .createdBy(po.getCreatedBy())
                .items(itemDtos)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
}
