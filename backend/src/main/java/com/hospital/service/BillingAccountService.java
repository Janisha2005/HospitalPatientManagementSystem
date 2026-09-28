package com.hospital.service;

import com.hospital.dto.BillingAccountDto;
import com.hospital.entity.BillingAccount;
import com.hospital.entity.BillingAccountStatus;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.BillingAccountRepository;
import com.hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BillingAccountService {

    private final BillingAccountRepository accountRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public BillingAccount getOrCreateAccountForPatient(Long patientId) {
        return accountRepository.findByPatientId(patientId).orElseGet(() -> {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));
            String accountNumber = generateAccountNumber();
            BillingAccount acc = BillingAccount.builder()
                    .accountNumber(accountNumber)
                    .patient(patient)
                    .accountStatus(BillingAccountStatus.ACTIVE)
                    .creditLimit(new BigDecimal("10000.00"))
                    .currentBalance(BigDecimal.ZERO)
                    .build();
            return accountRepository.save(acc);
        });
    }

    @Transactional
    public BillingAccountDto getAccountByPatientId(Long patientId) {
        BillingAccount acc = getOrCreateAccountForPatient(patientId);
        return mapToDto(acc);
    }

    private String generateAccountNumber() {
        long count = accountRepository.count() + 1;
        return String.format("ACC-%d-%06d", LocalDate.now().getYear(), count);
    }

    public BillingAccountDto mapToDto(BillingAccount a) {
        if (a == null) return null;
        return BillingAccountDto.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName())
                .patientCode(a.getPatient().getPatientId())
                .accountStatus(a.getAccountStatus())
                .creditLimit(a.getCreditLimit())
                .currentBalance(a.getCurrentBalance())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
