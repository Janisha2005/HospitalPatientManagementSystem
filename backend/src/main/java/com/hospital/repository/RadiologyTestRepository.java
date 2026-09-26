package com.hospital.repository;

import com.hospital.entity.RadiologyTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RadiologyTestRepository extends JpaRepository<RadiologyTest, Long> {
    Optional<RadiologyTest> findByTestCode(String testCode);
    boolean existsByTestCode(String testCode);
    List<RadiologyTest> findByIsActiveTrue();
    Page<RadiologyTest> findByIsActiveTrue(Pageable pageable);
}
