package com.hospital.repository;

import com.hospital.entity.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingAccountRepository extends JpaRepository<BillingAccount, Long> {
    Optional<BillingAccount> findByAccountNumber(String accountNumber);
    Optional<BillingAccount> findByPatientId(Long patientId);
    boolean existsByAccountNumber(String accountNumber);
}
