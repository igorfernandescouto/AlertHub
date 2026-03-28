package com.alerthub.tenant.api.dto;

import com.alerthub.tenant.domain.TenantStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateTenantStatusRequest(@NotNull TenantStatus status) {
}
