package com.alerthub.event.domain;

import java.time.Instant;
import java.util.Objects;

import com.alerthub.shared.domain.BaseEntity;
import com.alerthub.tenant.domain.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_records")
public class EventRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false, length = 120)
    private String source;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EventStatus status;

    @Column(name = "processing_attempts", nullable = false)
    private int processingAttempts;

    @Column(name = "last_error", length = 400)
    private String lastError;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected EventRecord() {
    }

    private EventRecord(Tenant tenant, String eventType, String source, String payload, Instant occurredAt, String idempotencyKey) {
        this.tenant = Objects.requireNonNull(tenant, "tenant is required");
        this.eventType = requireValue(eventType, "eventType");
        this.source = requireValue(source, "source");
        this.payload = requireValue(payload, "payload");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
        this.idempotencyKey = requireValue(idempotencyKey, "idempotencyKey");
        this.status = EventStatus.RECEIVED;
        this.processingAttempts = 0;
    }

    public static EventRecord create(
            Tenant tenant,
            String eventType,
            String source,
            String payload,
            Instant occurredAt,
            String idempotencyKey
    ) {
        return new EventRecord(tenant, eventType, source, payload, occurredAt, idempotencyKey);
    }

    public void markProcessing() {
        status = EventStatus.PROCESSING;
        processingAttempts++;
        lastError = null;
    }

    public void markProcessed() {
        status = EventStatus.PROCESSED;
        processedAt = Instant.now();
        lastError = null;
    }

    public void markFailed(String errorMessage) {
        status = EventStatus.FAILED;
        lastError = errorMessage;
    }

    public boolean isProcessed() {
        return status == EventStatus.PROCESSED;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSource() {
        return source;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public EventStatus getStatus() {
        return status;
    }

    public int getProcessingAttempts() {
        return processingAttempts;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    private static String requireValue(String value, String fieldName) {
        String sanitized = value == null ? "" : value.trim();
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return sanitized;
    }
}
