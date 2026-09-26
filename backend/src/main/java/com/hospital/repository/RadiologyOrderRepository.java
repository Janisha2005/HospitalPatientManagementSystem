package com.hospital.repository;

import com.hospital.entity.RadiologyOrder;
import com.hospital.entity.RadiologyOrderStatus;
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
public interface RadiologyOrderRepository extends JpaRepository<RadiologyOrder, Long> {
    Optional<RadiologyOrder> findByRadiologyOrderId(String radiologyOrderId);
    List<RadiologyOrder> findByPatientIdOrderByOrderDateDesc(Long patientId);
    List<RadiologyOrder> findByStatus(RadiologyOrderStatus status);
    long countByStatus(RadiologyOrderStatus status);
    long countByOrderDate(LocalDate date);
    long countByOrderDateAndStatus(LocalDate date, RadiologyOrderStatus status);

    @Query("SELECT r FROM RadiologyOrder r WHERE " +
           "(:patientId IS NULL OR r.patient.id = :patientId) AND " +
           "(:doctorId IS NULL OR r.doctor.id = :doctorId) AND " +
           "(:departmentId IS NULL OR r.department.id = :departmentId) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<RadiologyOrder> findOrdersWithFilters(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("departmentId") Long departmentId,
            @Param("status") RadiologyOrderStatus status,
            Pageable pageable
    );
}
