package com.alerthub.notification.infrastructure;

import java.util.UUID;

import com.alerthub.notification.domain.NotificationChannel;
import com.alerthub.notification.domain.NotificationRecord;
import com.alerthub.notification.domain.NotificationStatus;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationRecordRepository implements PanacheRepositoryBase<NotificationRecord, UUID> {

    public PanacheQuery<NotificationRecord> search(
            UUID tenantId,
            NotificationStatus status,
            NotificationChannel channel,
            int page,
            int size
    ) {
        StringBuilder query = new StringBuilder("tenant.id = ?1");
        Object[] parameters = new Object[] { tenantId, status, channel };
        int nextIndex = 2;

        if (status != null) {
            query.append(" and status = ?").append(nextIndex++);
        }
        if (channel != null) {
            query.append(" and channel = ?").append(nextIndex);
        }

        return find(query.toString(), Sort.by("createdAt").descending(), compact(parameters))
                .page(Page.of(page, size));
    }

    private Object[] compact(Object[] parameters) {
        return java.util.Arrays.stream(parameters)
                .filter(java.util.Objects::nonNull)
                .toArray();
    }
}
