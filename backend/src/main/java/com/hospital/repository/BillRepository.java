package com.hospital.repository;

import com.hospital.entity.Bill;
import com.hospital.entity.BillStatus;
import com.hospital.entity.BillType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillNumber(String billNumber);

    List<Bill> findByPatientId(Long patientId);
    Page<Bill> findByPatientId(Long patientId, Pageable pageable);

    List<Bill> findByOpdVisitId(Long opdVisitId);
    List<Bill> findByIpdAdmissionId(Long ipdAdmissionId);

    Page<Bill> findByStatus(BillStatus status, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE b.billDate BETWEEN :startDate AND :endDate")
    List<Bill> findByBillDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(b.grandTotal), 0) FROM Bill b WHERE b.billDate = :date AND b.status != 'CANCELLED'")
    BigDecimal getDailyBilledAmount(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Bill b WHERE b.billDate = :date AND b.status != 'CANCELLED'")
    BigDecimal getDailyCollectedAmount(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(b.outstandingAmount), 0) FROM Bill b WHERE b.status IN ('GENERATED', 'PARTIALLY_PAID')")
    BigDecimal getTotalOutstandingAmount();

    @Query("SELECT COALESCE(SUM(b.grandTotal), 0) FROM Bill b WHERE b.billType = :billType AND b.status != 'CANCELLED'")
    BigDecimal getTotalRevenueByBillType(@Param("billType") BillType billType);

    @Query("SELECT b FROM Bill b WHERE LOWER(b.billNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.patient.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.patient.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.patient.patientId) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Bill> searchBills(@Param("query") String query, Pageable pageable);
}
