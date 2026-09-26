package com.hospital.service;

import com.hospital.dto.GoodsReceiptDto;
import com.hospital.dto.GoodsReceiptItemDto;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final InventoryService inventoryService;
    private final BatchService batchService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<GoodsReceiptDto> getAllGoodsReceipts(Pageable pageable) {
        return goodsReceiptRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public GoodsReceiptDto getGoodsReceiptById(Long id) {
        GoodsReceipt gr = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt not found with ID: " + id));
        return mapToDto(gr);
    }

    @Transactional
    public GoodsReceiptDto createGoodsReceipt(GoodsReceiptDto dto, String username) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + dto.getSupplierId()));

        PurchaseOrder purchaseOrder = null;
        if (dto.getPurchaseOrderId() != null) {
            purchaseOrder = purchaseOrderRepository.findById(dto.getPurchaseOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + dto.getPurchaseOrderId()));
        }

        String grnId = generateGoodsReceiptId();

        GoodsReceipt gr = GoodsReceipt.builder()
                .goodsReceiptId(grnId)
                .purchaseOrder(purchaseOrder)
                .supplier(supplier)
                .receiptDate(dto.getReceiptDate() != null ? dto.getReceiptDate() : LocalDate.now())
                .invoiceNumber(dto.getInvoiceNumber())
                .invoiceDate(dto.getInvoiceDate())
                .receivedBy(username)
                .remarks(dto.getRemarks())
                .build();

        GoodsReceipt savedGr = goodsReceiptRepository.save(gr);
        List<GoodsReceiptItem> receiptItems = new ArrayList<>();

        for (GoodsReceiptItemDto itemDto : dto.getItems()) {
            Medicine medicine = medicineRepository.findById(itemDto.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + itemDto.getMedicineId()));

            if (itemDto.getExpiryDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Cannot receive expired stock for batch: " + itemDto.getBatchNumber());
            }

            // Find or create batch
            MedicineBatch batch;
            List<MedicineBatch> existingBatches = batchRepository.findByMedicineId(medicine.getId());
            MedicineBatch matchingBatch = existingBatches.stream()
                    .filter(b -> b.getBatchNumber().equalsIgnoreCase(itemDto.getBatchNumber()))
                    .findFirst().orElse(null);

            if (matchingBatch != null) {
                batch = batchRepository.findByIdForUpdate(matchingBatch.getId()).orElse(matchingBatch);
                batch.setQuantityReceived(batch.getQuantityReceived() + itemDto.getQuantityReceived());
                batch.setQuantityAvailable(batch.getQuantityAvailable() + itemDto.getQuantityReceived());
                batch.setPurchaseRate(itemDto.getPurchaseRate());
                if (itemDto.getMrp() != null) batch.setMrp(itemDto.getMrp());
                batch.setStatus(batchService.evaluateBatchStatus(batch.getExpiryDate(), batch.getQuantityAvailable()));
                batchRepository.save(batch);
            } else {
                String batchId = String.format("BAT-%d-%06d", LocalDate.now().getYear(), batchRepository.count() + 1);
                BatchStatus initialStatus = batchService.evaluateBatchStatus(itemDto.getExpiryDate(), itemDto.getQuantityReceived());
                batch = MedicineBatch.builder()
                        .batchId(batchId)
                        .medicine(medicine)
                        .supplier(supplier)
                        .batchNumber(itemDto.getBatchNumber())
                        .expiryDate(itemDto.getExpiryDate())
                        .purchaseRate(itemDto.getPurchaseRate())
                        .mrp(itemDto.getMrp())
                        .sellingRate(itemDto.getMrp())
                        .quantityReceived(itemDto.getQuantityReceived())
                        .quantityAvailable(itemDto.getQuantityReceived())
                        .quantityReserved(0)
                        .quantityDamaged(0)
                        .quantityExpired(0)
                        .status(initialStatus)
                        .build();
                batch = batchRepository.save(batch);
            }

            // Record inventory transaction
            inventoryService.recordTransaction(medicine, batch, InventoryTransactionType.PURCHASE_RECEIPT,
                    itemDto.getQuantityReceived(), itemDto.getPurchaseRate(), "GOODS_RECEIPT",
                    savedGr.getGoodsReceiptId(), "Goods received via GRN: " + savedGr.getGoodsReceiptId(), username);

            GoodsReceiptItem gri = GoodsReceiptItem.builder()
                    .goodsReceipt(savedGr)
                    .medicine(medicine)
                    .batch(batch)
                    .quantityReceived(itemDto.getQuantityReceived())
                    .purchaseRate(itemDto.getPurchaseRate())
                    .mrp(itemDto.getMrp())
                    .expiryDate(itemDto.getExpiryDate())
                    .build();

            receiptItems.add(gri);

            // Update Purchase Order Item if linked
            if (purchaseOrder != null) {
                List<PurchaseOrderItem> poItems = purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrder.getId());
                poItems.stream()
                        .filter(poi -> poi.getMedicine().getId().equals(medicine.getId()))
                        .findFirst()
                        .ifPresent(poi -> {
                            poi.setReceivedQuantity(poi.getReceivedQuantity() + itemDto.getQuantityReceived());
                            purchaseOrderItemRepository.save(poi);
                        });
            }
        }

        goodsReceiptItemRepository.saveAll(receiptItems);

        // Update Purchase Order status if linked
        if (purchaseOrder != null) {
            List<PurchaseOrderItem> poItems = purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrder.getId());
            boolean allFullyReceived = poItems.stream().allMatch(i -> i.getReceivedQuantity() >= i.getOrderedQuantity());
            boolean anyReceived = poItems.stream().anyMatch(i -> i.getReceivedQuantity() > 0);

            if (allFullyReceived) {
                purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
            } else if (anyReceived) {
                purchaseOrder.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
            }
            purchaseOrderRepository.save(purchaseOrder);
        }

        auditLogService.logAction("CREATE_GOODS_RECEIPT", "GoodsReceipt", savedGr.getId().toString(),
                "Created Goods Receipt: " + savedGr.getGoodsReceiptId() + " for Supplier: " + supplier.getSupplierName());

        return mapToDto(savedGr);
    }

    private String generateGoodsReceiptId() {
        long count = goodsReceiptRepository.count() + 1;
        return String.format("GRN-%d-%06d", LocalDate.now().getYear(), count);
    }

    public GoodsReceiptDto mapToDto(GoodsReceipt gr) {
        if (gr == null) return null;

        List<GoodsReceiptItem> items = goodsReceiptItemRepository.findByGoodsReceiptId(gr.getId());
        List<GoodsReceiptItemDto> itemDtos = items.stream().map(i -> GoodsReceiptItemDto.builder()
                .id(i.getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getMedicineName())
                .batchId(i.getBatch().getId())
                .batchNumber(i.getBatch().getBatchNumber())
                .quantityReceived(i.getQuantityReceived())
                .purchaseRate(i.getPurchaseRate())
                .mrp(i.getMrp())
                .expiryDate(i.getExpiryDate())
                .build()).collect(Collectors.toList());

        return GoodsReceiptDto.builder()
                .id(gr.getId())
                .goodsReceiptId(gr.getGoodsReceiptId())
                .purchaseOrderId(gr.getPurchaseOrder() != null ? gr.getPurchaseOrder().getId() : null)
                .purchaseOrderNumber(gr.getPurchaseOrder() != null ? gr.getPurchaseOrder().getPurchaseOrderId() : null)
                .supplierId(gr.getSupplier().getId())
                .supplierName(gr.getSupplier().getSupplierName())
                .receiptDate(gr.getReceiptDate())
                .invoiceNumber(gr.getInvoiceNumber())
                .invoiceDate(gr.getInvoiceDate())
                .receivedBy(gr.getReceivedBy())
                .remarks(gr.getRemarks())
                .items(itemDtos)
                .createdAt(gr.getCreatedAt())
                .build();
    }
}
