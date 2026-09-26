package com.hospital.repository;

import com.hospital.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    Optional<Department> findByDepartmentCode(String departmentCode);
    boolean existsByDepartmentCode(String departmentCode);
    boolean existsByDepartmentName(String departmentName);
    long countByIsActiveTrue();
}
