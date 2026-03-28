package com.alerthub.tenant.api.dto;

import com.alerthub.tenant.domain.TenantStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotBlank
        @Size(max = 80)
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "slug must contain lowercase letters, numbers, and hyphens only"
        )
        String slug,

        @NotNull
        TenantStatus status
) {
}
