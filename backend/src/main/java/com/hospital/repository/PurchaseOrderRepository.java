package com.hospital.repository;

import com.hospital.entity.PurchaseOrder;
import com.hospital.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByPurchaseOrderId(String purchaseOrderId);
    Page<PurchaseOrder> findBySupplierId(Long supplierId, Pageable pageable);
    Page<PurchaseOrder> findByStatus(PurchaseOrderStatus status, Pageable pageable);
    List<PurchaseOrder> findByStatusIn(List<PurchaseOrderStatus> statuses);
    long countByStatus(PurchaseOrderStatus status);
}
