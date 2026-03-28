package com.alerthub.event.infrastructure;

import java.util.Optional;
import java.util.UUID;

import com.alerthub.event.domain.EventRecord;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventRecordRepository implements PanacheRepositoryBase<EventRecord, UUID> {

    public boolean existsByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey) {
        return count("tenant.id = ?1 and idempotencyKey = ?2", tenantId, idempotencyKey) > 0;
    }

    public Optional<EventRecord> findByTenantIdAndId(UUID tenantId, UUID eventId) {
        return find("tenant.id = ?1 and id = ?2", tenantId, eventId).firstResultOptional();
    }

    public Optional<EventRecord> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey) {
        return find("tenant.id = ?1 and idempotencyKey = ?2", tenantId, idempotencyKey).firstResultOptional();
    }

    public PanacheQuery<EventRecord> findByTenantId(UUID tenantId, int page, int size) {
        return find("tenant.id", Sort.by("createdAt").descending(), tenantId)
                .page(Page.of(page, size));
    }
}
