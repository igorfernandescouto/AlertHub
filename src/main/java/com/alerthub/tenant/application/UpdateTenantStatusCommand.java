package com.alerthub.tenant.application;

import java.util.Objects;

import com.alerthub.tenant.domain.TenantStatus;

public record UpdateTenantStatusCommand(TenantStatus status) {

    public UpdateTenantStatusCommand {
        Objects.requireNonNull(status, "status is required");
    }
}
