package com.alerthub.tenant.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.alerthub.tenant.domain.Tenant;
import com.alerthub.tenant.domain.TenantStatus;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
