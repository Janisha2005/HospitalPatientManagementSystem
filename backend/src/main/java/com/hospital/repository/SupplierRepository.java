package com.hospital.repository;

import com.hospital.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);
    boolean existsBySupplierCode(String supplierCode);
    boolean existsByGstNumber(String gstNumber);
    boolean existsByDrugLicenseNumber(String drugLicenseNumber);
    List<Supplier> findByIsActiveTrue();

    @Query("SELECT s FROM Supplier s WHERE LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :query, '%')) OR s.phone LIKE CONCAT('%', :query, '%')")
    Page<Supplier> searchSuppliers(@Param("query") String query, Pageable pageable);
}
