package com.alerthub.audit.domain;

import java.util.Objects;
import java.util.UUID;

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
@Table(name = "audit_entries")
public class AuditEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 80)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(columnDefinition = "CLOB")
    private String details;

    protected AuditEntry() {
    }

    private AuditEntry(Tenant tenant, UUID actorUserId, AuditAction action, String entityType, UUID entityId, String details) {
        this.tenant = tenant;
        this.actorUserId = actorUserId;
        this.action = Objects.requireNonNull(action, "action is required");
        this.entityType = Objects.requireNonNull(entityType, "entity type is required");
        this.entityId = entityId;
        this.details = details;
    }

    public static AuditEntry create(
            Tenant tenant,
            UUID actorUserId,
            AuditAction action,
            String entityType,
            UUID entityId,
            String details
    ) {
        return new AuditEntry(tenant, actorUserId, action, entityType, entityId, details);
    }

    public Tenant getTenant() {
        return tenant;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }
}
