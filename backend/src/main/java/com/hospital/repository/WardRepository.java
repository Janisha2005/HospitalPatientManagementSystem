package com.hospital.repository;

import com.hospital.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long>, JpaSpecificationExecutor<Ward> {
    Optional<Ward> findByWardCode(String wardCode);
    boolean existsByWardCode(String wardCode);
    List<Ward> findByDepartmentIdAndIsActiveTrue(Long departmentId);
    List<Ward> findByIsActiveTrue();
    long countByIsActiveTrue();
}
