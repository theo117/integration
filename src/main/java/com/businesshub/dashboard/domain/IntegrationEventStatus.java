package com.businesshub.dashboard.domain;

public enum IntegrationEventStatus {
    RECEIVED,
    PROCESSED,
    SENT,
    FAILED,
    SKIPPED,
    PENDING_RETRY
}
