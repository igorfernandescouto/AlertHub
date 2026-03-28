package com.alerthub.audit.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.alerthub.audit.domain.AuditAction;
import com.alerthub.audit.domain.AuditEntry;

public record AuditEntryResponse(
        UUID id,
        UUID tenantId,
        UUID actorUserId,
        AuditAction action,
        String entityType,
        UUID entityId,
        String details,
        Instant createdAt
) {

    public static AuditEntryResponse from(AuditEntry entry) {
        return new AuditEntryResponse(
                entry.getId(),
                entry.getTenant() == null ? null : entry.getTenant().getId(),
                entry.getActorUserId(),
                entry.getAction(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getDetails(),
                entry.getCreatedAt()
        );
    }
}
