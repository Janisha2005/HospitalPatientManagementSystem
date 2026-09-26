package com.hospital.repository;

import com.hospital.entity.Bed;
import com.hospital.entity.BedStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long>, JpaSpecificationExecutor<Bed> {
    Optional<Bed> findByBedCode(String bedCode);
    boolean existsByBedCode(String bedCode);
    boolean existsByWardIdAndBedNumber(Long wardId, String bedNumber);

    List<Bed> findByWardIdAndIsActiveTrue(Long wardId);
    List<Bed> findByStatusAndIsActiveTrue(BedStatus status);
    List<Bed> findByWardIdAndStatusAndIsActiveTrue(Long wardId, BedStatus status);

    long countByStatus(BedStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bed b WHERE b.id = :id")
    Optional<Bed> findByIdForUpdate(@Param("id") Long id);
}
