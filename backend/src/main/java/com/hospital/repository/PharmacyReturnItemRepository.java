package com.hospital.repository;

import com.hospital.entity.PharmacyReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PharmacyReturnItemRepository extends JpaRepository<PharmacyReturnItem, Long> {
    List<PharmacyReturnItem> findByPharmacyReturnId(Long pharmacyReturnId);
}
