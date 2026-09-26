package com.hospital.repository;

import com.hospital.entity.MedicineCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineCategoryRepository extends JpaRepository<MedicineCategory, Long> {
    Optional<MedicineCategory> findByCategoryCode(String categoryCode);
    boolean existsByCategoryCode(String categoryCode);
    List<MedicineCategory> findByIsActiveTrue();
    Page<MedicineCategory> findByCategoryNameContainingIgnoreCaseOrCategoryCodeContainingIgnoreCase(String name, String code, Pageable pageable);
}
