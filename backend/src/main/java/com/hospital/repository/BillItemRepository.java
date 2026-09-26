package com.hospital.repository;

import com.hospital.entity.BillItem;
import com.hospital.entity.BillSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    List<BillItem> findByBillId(Long billId);
    boolean existsBySourceTypeAndSourceId(BillSourceType sourceType, Long sourceId);
    List<BillItem> findBySourceTypeAndSourceId(BillSourceType sourceType, Long sourceId);
}
