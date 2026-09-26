package com.hospital.repository;

import com.hospital.entity.BatchStatus;
import com.hospital.entity.MedicineBatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, Long> {
    Optional<MedicineBatch> findByBatchId(String batchId);
    Optional<MedicineBatch> findByBatchNumber(String batchNumber);
    List<MedicineBatch> findByMedicineId(Long medicineId);
    List<MedicineBatch> findByMedicineIdAndStatus(Long medicineId, BatchStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM MedicineBatch b WHERE b.id = :id")
    Optional<MedicineBatch> findByIdForUpdate(@Param("id") Long id);

    // FEFO: First Expiry First Out
    @Query("SELECT b FROM MedicineBatch b WHERE b.medicine.id = :medicineId AND b.status = 'AVAILABLE' AND b.quantityAvailable > 0 AND b.expiryDate >= :today ORDER BY b.expiryDate ASC")
    List<MedicineBatch> findFefoBatchesForMedicine(@Param("medicineId") Long medicineId, @Param("today") LocalDate today);

    @Query("SELECT b FROM MedicineBatch b WHERE b.expiryDate BETWEEN :startDate AND :endDate AND b.status != 'EXPIRED'")
    List<MedicineBatch> findBatchesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM MedicineBatch b WHERE b.expiryDate < :today AND b.status != 'EXPIRED'")
    List<MedicineBatch> findExpiredBatches(@Param("today") LocalDate today);

    Page<MedicineBatch> findByStatus(BatchStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.quantityAvailable), 0) FROM MedicineBatch b WHERE b.medicine.id = :medicineId AND b.status = 'AVAILABLE'")
    Integer getTotalAvailableQuantityForMedicine(@Param("medicineId") Long medicineId);

    @Query("SELECT b FROM MedicineBatch b WHERE LOWER(b.batchNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.medicine.medicineName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.batchId) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<MedicineBatch> searchBatches(@Param("query") String query, Pageable pageable);
}
