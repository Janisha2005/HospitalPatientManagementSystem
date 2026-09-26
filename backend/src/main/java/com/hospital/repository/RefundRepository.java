package com.hospital.repository;

import com.hospital.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByRefundNumber(String refundNumber);
    List<Refund> findByBillId(Long billId);
    List<Refund> findByPatientId(Long patientId);
    Page<Refund> findByPatientId(Long patientId, Pageable pageable);
}
