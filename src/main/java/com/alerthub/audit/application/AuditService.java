package com.alerthub.audit.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alerthub.audit.api.dto.AuditEntryResponse;
import com.alerthub.audit.domain.AuditAction;
import com.alerthub.audit.domain.AuditEntry;
import com.alerthub.audit.infrastructure.AuditEntryRepository;
import com.alerthub.shared.paging.PagedResponse;
import com.alerthub.tenant.domain.Tenant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuditService {

    private final AuditEntryRepository auditEntryRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEntryRepository auditEntryRepository, ObjectMapper objectMapper) {
        this.auditEntryRepository = auditEntryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void record(
            Tenant tenant,
            UUID actorUserId,
            AuditAction action,
            String entityType,
            UUID entityId,
            Map<String, Object> details
    ) {
        AuditEntry entry = AuditEntry.create(
                tenant,
                actorUserId,
                action,
                entityType,
                entityId,
                serialize(details)
        );
        auditEntryRepository.persist(entry);
    }

    public PagedResponse<AuditEntryResponse> search(UUID tenantId, AuditAction action, Instant from, Instant to, int page, int size) {
        var query = auditEntryRepository.search(tenantId, action, from, to, page, size);
        List<AuditEntryResponse> items = query.list().stream()
                .map(AuditEntryResponse::from)
                .toList();

        return PagedResponse.of(items, page, size, query.count());
    }

    private String serialize(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("could not serialize audit details");
        }
    }
}
