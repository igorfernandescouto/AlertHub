package com.alerthub.event.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.alerthub.event.domain.EventRecord;
import com.alerthub.event.domain.EventStatus;

public record EventResponse(
        UUID id,
        UUID tenantId,
        String eventType,
        String source,
        String payload,
        Instant occurredAt,
        String idempotencyKey,
        EventStatus status,
        int processingAttempts,
        String lastError,
        Instant processedAt,
        Instant createdAt
) {

    public static EventResponse from(EventRecord event) {
        return new EventResponse(
                event.getId(),
                event.getTenant().getId(),
                event.getEventType(),
                event.getSource(),
                event.getPayload(),
                event.getOccurredAt(),
                event.getIdempotencyKey(),
                event.getStatus(),
                event.getProcessingAttempts(),
                event.getLastError(),
                event.getProcessedAt(),
                event.getCreatedAt()
        );
    }
}
