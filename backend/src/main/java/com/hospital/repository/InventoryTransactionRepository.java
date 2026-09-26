package com.hospital.repository;

import com.hospital.entity.InventoryTransaction;
import com.hospital.entity.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Optional<InventoryTransaction> findByTransactionId(String transactionId);
    Page<InventoryTransaction> findByMedicineIdOrderByTransactionDatetimeDesc(Long medicineId, Pageable pageable);
    Page<InventoryTransaction> findByBatchIdOrderByTransactionDatetimeDesc(Long batchId, Pageable pageable);
    Page<InventoryTransaction> findByTransactionTypeOrderByTransactionDatetimeDesc(InventoryTransactionType type, Pageable pageable);
    List<InventoryTransaction> findTop10ByOrderByTransactionDatetimeDesc();
}
