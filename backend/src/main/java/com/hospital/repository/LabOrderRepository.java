package com.hospital.repository;

import com.hospital.entity.LabOrder;
import com.hospital.entity.LabOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    Optional<LabOrder> findByLabOrderId(String labOrderId);
    List<LabOrder> findByPatientIdOrderByOrderDateDesc(Long patientId);
    List<LabOrder> findByStatus(LabOrderStatus status);
    long countByStatus(LabOrderStatus status);
    long countByOrderDate(LocalDate date);
    long countByOrderDateAndStatus(LocalDate date, LabOrderStatus status);

    @Query("SELECT l FROM LabOrder l WHERE " +
           "(:patientId IS NULL OR l.patient.id = :patientId) AND " +
           "(:doctorId IS NULL OR l.doctor.id = :doctorId) AND " +
           "(:departmentId IS NULL OR l.department.id = :departmentId) AND " +
           "(:status IS NULL OR l.status = :status)")
    Page<LabOrder> findLabOrdersWithFilters(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("departmentId") Long departmentId,
            @Param("status") LabOrderStatus status,
            Pageable pageable
    );
}
