package com.alerthub.audit.infrastructure;

import java.time.Instant;
import java.util.UUID;

import com.alerthub.audit.domain.AuditAction;
import com.alerthub.audit.domain.AuditEntry;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditEntryRepository implements PanacheRepositoryBase<AuditEntry, UUID> {

    public PanacheQuery<AuditEntry> search(UUID tenantId, AuditAction action, Instant from, Instant to, int page, int size) {
        StringBuilder query = new StringBuilder("tenant.id = ?1");
        Object[] parameters = new Object[] { tenantId, action, from, to };
        int nextIndex = 2;

        if (action != null) {
            query.append(" and action = ?").append(nextIndex++);
        }
        if (from != null) {
            query.append(" and createdAt >= ?").append(nextIndex++);
        }
        if (to != null) {
            query.append(" and createdAt <= ?").append(nextIndex);
        }

        PanacheQuery<AuditEntry> panacheQuery = find(query.toString(), Sort.by("createdAt").descending(), compact(parameters));
        return panacheQuery.page(Page.of(page, size));
    }

    private Object[] compact(Object[] parameters) {
        return java.util.Arrays.stream(parameters)
                .filter(java.util.Objects::nonNull)
                .toArray();
    }
}
