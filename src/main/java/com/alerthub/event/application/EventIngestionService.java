package com.alerthub.event.application;

import java.util.Map;
import java.util.UUID;

import com.alerthub.audit.application.AuditService;
import com.alerthub.audit.domain.AuditAction;
import com.alerthub.event.api.dto.EventResponse;
import com.alerthub.event.domain.EventRecord;
import com.alerthub.event.infrastructure.EventRecordRepository;
import com.alerthub.shared.exception.ConflictException;
import com.alerthub.shared.exception.ResourceNotFoundException;
import com.alerthub.shared.paging.PagedResponse;
import com.alerthub.shared.security.TenantAccessVerifier;
import com.alerthub.tenant.domain.Tenant;
import com.alerthub.tenant.infrastructure.TenantRepository;
import com.alerthub.user.domain.UserRole;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EventIngestionService {

    private final EventRecordRepository eventRecordRepository;
    private final TenantRepository tenantRepository;
    private final TenantAccessVerifier tenantAccessVerifier;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;
    private final AuditService auditService;

    public EventIngestionService(
            EventRecordRepository eventRecordRepository,
            TenantRepository tenantRepository,
            TenantAccessVerifier tenantAccessVerifier,
            ObjectMapper objectMapper,
            EventPublisher eventPublisher,
            AuditService auditService
    ) {
        this.eventRecordRepository = eventRecordRepository;
        this.tenantRepository = tenantRepository;
        this.tenantAccessVerifier = tenantAccessVerifier;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    @Transactional
    public EventResponse ingest(CreateEventCommand command) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR);

        Tenant tenant = requireTenant(tenantId);
        assertUniqueIdempotency(tenantId, command.idempotencyKey());

        EventRecord event = EventRecord.create(
                tenant,
                command.eventType(),
                command.source(),
                serialize(command.payload()),
                command.occurredAt(),
                command.idempotencyKey()
        );
        eventRecordRepository.persist(event);

        auditService.record(
                tenant,
                tenantAccessVerifier.currentUserId(),
                AuditAction.EVENT_RECEIVED,
                "EventRecord",
                event.getId(),
                Map.of("eventType", event.getEventType(), "source", event.getSource())
        );

        eventPublisher.publish(event.getId());
        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse reprocess(UUID eventId) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR);

        EventRecord event = requireEvent(tenantId, eventId);

        auditService.record(
                event.getTenant(),
                tenantAccessVerifier.currentUserId(),
                AuditAction.EVENT_REPROCESSED,
                "EventRecord",
                event.getId(),
                Map.of("status", event.getStatus().name())
        );

        eventPublisher.publish(event.getId());
        return EventResponse.from(event);
    }

    public PagedResponse<EventResponse> list(int page, int size) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);

        var query = eventRecordRepository.findByTenantId(tenantId, page, size);
        return PagedResponse.of(
                query.list().stream().map(EventResponse::from).toList(),
                page,
                size,
                query.count()
        );
    }

    private void assertUniqueIdempotency(UUID tenantId, String idempotencyKey) {
        if (!eventRecordRepository.existsByTenantIdAndIdempotencyKey(tenantId, idempotencyKey)) {
            return;
        }
        throw new ConflictException("Idempotency key '%s' was already processed for the current tenant.".formatted(idempotencyKey));
    }

    private Tenant requireTenant(UUID tenantId) {
        return tenantRepository.findByIdOptional(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant %s was not found.".formatted(tenantId)));
    }

    private EventRecord requireEvent(UUID tenantId, UUID eventId) {
        return eventRecordRepository.findByTenantIdAndId(tenantId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event %s was not found.".formatted(eventId)));
    }

    private String serialize(com.fasterxml.jackson.databind.JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("payload could not be serialized");
        }
    }
}
