package com.alerthub.tenant.application;

import java.util.Objects;

import com.alerthub.tenant.domain.TenantStatus;

public record CreateTenantCommand(String name, String slug, TenantStatus status) {

    public CreateTenantCommand {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(slug, "slug is required");
        Objects.requireNonNull(status, "status is required");
    }
}
