package com.hospital.repository;

import com.hospital.entity.GoodsReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Optional<GoodsReceipt> findByGoodsReceiptId(String goodsReceiptId);
    List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);
    Page<GoodsReceipt> findBySupplierId(Long supplierId, Pageable pageable);
    long countByReceiptDate(LocalDate date);
}
