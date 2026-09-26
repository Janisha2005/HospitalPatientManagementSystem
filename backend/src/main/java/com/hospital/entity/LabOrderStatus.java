package com.hospital.entity;

public enum LabOrderStatus {
    ORDERED,
    SAMPLE_PENDING,
    SAMPLE_COLLECTED,
    PROCESSING,
    RESULT_READY,
    VERIFIED,
    COMPLETED,
    CANCELLED
}
