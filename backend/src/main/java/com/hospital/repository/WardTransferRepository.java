package com.hospital.repository;

import com.hospital.entity.WardTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardTransferRepository extends JpaRepository<WardTransfer, Long> {
    Optional<WardTransfer> findByTransferId(String transferId);
    boolean existsByTransferId(String transferId);
    List<WardTransfer> findByAdmissionIdOrderByTransferDatetimeDesc(Long admissionId);
    List<WardTransfer> findByPatientIdOrderByTransferDatetimeDesc(Long patientId);
}
