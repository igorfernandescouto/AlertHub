package com.alerthub.tenant.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alerthub.audit.application.AuditService;
import com.alerthub.audit.domain.AuditAction;
import com.alerthub.shared.exception.ConflictException;
import com.alerthub.shared.exception.ResourceNotFoundException;
import com.alerthub.shared.security.TenantAccessVerifier;
import com.alerthub.tenant.api.dto.TenantResponse;
import com.alerthub.tenant.domain.Tenant;
import com.alerthub.tenant.infrastructure.TenantRepository;
import com.alerthub.user.domain.UserRole;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantAccessVerifier tenantAccessVerifier;
    private final AuditService auditService;

    public TenantService(TenantRepository tenantRepository, TenantAccessVerifier tenantAccessVerifier, AuditService auditService) {
        this.tenantRepository = tenantRepository;
        this.tenantAccessVerifier = tenantAccessVerifier;
        this.auditService = auditService;
    }

    @Transactional
    public TenantResponse create(CreateTenantCommand command) {
        validateUniqueSlug(command.slug());

        Tenant tenant = Tenant.create(command.name(), command.slug(), command.status());
        tenantRepository.persist(tenant);

        auditService.record(
                tenant,
                null,
                AuditAction.TENANT_CREATED,
                "Tenant",
                tenant.getId(),
                Map.of("slug", tenant.getSlug(), "status", tenant.getStatus().name())
        );

        return TenantResponse.from(tenant);
    }

    public List<TenantResponse> list() {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);

        return List.of(TenantResponse.from(requireTenant(tenantId)));
    }

    public TenantResponse findById(UUID tenantId) {
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);
        return TenantResponse.from(requireTenant(tenantId));
    }

    @Transactional
    public TenantResponse updateStatus(UUID tenantId, UpdateTenantStatusCommand command) {
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN);
        Tenant tenant = requireTenant(tenantId);
        tenant.updateStatus(command.status());

        auditService.record(
                tenant,
                tenantAccessVerifier.currentUserId(),
                AuditAction.TENANT_STATUS_UPDATED,
                "Tenant",
                tenant.getId(),
                Map.of("status", tenant.getStatus().name())
        );

        return TenantResponse.from(tenant);
    }

    private Tenant requireTenant(UUID tenantId) {
        return tenantRepository.findByIdOptional(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant %s was not found.".formatted(tenantId)));
    }

    private void validateUniqueSlug(String slug) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new ConflictException("Tenant slug '%s' is already in use.".formatted(slug));
        }
    }
}
