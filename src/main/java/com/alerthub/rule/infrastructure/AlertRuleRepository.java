package com.alerthub.rule.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.alerthub.rule.domain.AlertRule;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertRuleRepository implements PanacheRepositoryBase<AlertRule, UUID> {

    public Optional<AlertRule> findByTenantIdAndId(UUID tenantId, UUID ruleId) {
        return find("tenant.id = ?1 and id = ?2", tenantId, ruleId).firstResultOptional();
    }

    public List<AlertRule> listActiveByTenantIdAndEventType(UUID tenantId, String eventType) {
        return list("tenant.id = ?1 and eventType = ?2 and active = true", Sort.by("priority").descending(), tenantId, eventType);
    }

    public PanacheQuery<AlertRule> findByTenantId(UUID tenantId, int page, int size) {
        return find("tenant.id", Sort.by("createdAt").descending(), tenantId)
                .page(Page.of(page, size));
    }
}
