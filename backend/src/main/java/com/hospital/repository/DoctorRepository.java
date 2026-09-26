package com.hospital.repository;

import com.hospital.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByDoctorId(String doctorId);
    Optional<Doctor> findByEmail(String email);
    boolean existsByDoctorId(String doctorId);
    boolean existsByLicenseNumber(String licenseNumber);
    boolean existsByEmail(String email);
    long countByDepartmentId(Long departmentId);
    long countByIsActiveTrue();

    @Query("SELECT d FROM Doctor d WHERE " +
           "(:departmentId IS NULL OR d.department.id = :departmentId) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(d.doctorId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.specialization) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.phone) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:isActive IS NULL OR d.isActive = :isActive)")
    Page<Doctor> searchDoctors(@Param("query") String query,
                              @Param("departmentId") Long departmentId,
                              @Param("isActive") Boolean isActive,
                              Pageable pageable);
}
