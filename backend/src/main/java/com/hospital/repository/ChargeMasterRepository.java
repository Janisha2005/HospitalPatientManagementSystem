package com.hospital.repository;

import com.hospital.entity.ChargeCategory;
import com.hospital.entity.ChargeMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChargeMasterRepository extends JpaRepository<ChargeMaster, Long> {
    Optional<ChargeMaster> findByChargeCode(String chargeCode);
    boolean existsByChargeCode(String chargeCode);

    List<ChargeMaster> findByChargeCategoryAndIsActiveTrue(ChargeCategory category);
    List<ChargeMaster> findByIsActiveTrue();

    @Query("SELECT c FROM ChargeMaster c WHERE LOWER(c.chargeName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.chargeCode) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ChargeMaster> searchCharges(@Param("query") String query, Pageable pageable);
}
