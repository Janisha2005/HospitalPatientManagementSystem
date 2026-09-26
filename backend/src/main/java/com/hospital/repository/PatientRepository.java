package com.hospital.repository;

import com.hospital.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
    Optional<Patient> findByPatientId(String patientId);
    Optional<Patient> findByEmail(String email);
    boolean existsByPatientId(String patientId);
    long countByIsActiveTrue();

    @Query("SELECT p FROM Patient p WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(p.patientId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<Patient> searchPatients(@Param("query") String query, @Param("isActive") Boolean isActive, Pageable pageable);
}
