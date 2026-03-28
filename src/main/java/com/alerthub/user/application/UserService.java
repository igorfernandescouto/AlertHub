package com.alerthub.user.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alerthub.audit.application.AuditService;
import com.alerthub.audit.domain.AuditAction;
import com.alerthub.shared.exception.ConflictException;
import com.alerthub.shared.exception.ResourceNotFoundException;
import com.alerthub.shared.security.PasswordHasher;
import com.alerthub.shared.security.TenantAccessVerifier;
import com.alerthub.tenant.domain.Tenant;
import com.alerthub.tenant.infrastructure.TenantRepository;
import com.alerthub.user.api.dto.UserResponse;
import com.alerthub.user.domain.AppUser;
import com.alerthub.user.domain.UserRole;
import com.alerthub.user.domain.UserStatus;
import com.alerthub.user.infrastructure.AppUserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    private final AppUserRepository appUserRepository;
    private final TenantRepository tenantRepository;
    private final PasswordHasher passwordHasher;
    private final TenantAccessVerifier tenantAccessVerifier;
    private final AuditService auditService;

    public UserService(
            AppUserRepository appUserRepository,
            TenantRepository tenantRepository,
            PasswordHasher passwordHasher,
            TenantAccessVerifier tenantAccessVerifier,
            AuditService auditService
    ) {
        this.appUserRepository = appUserRepository;
        this.tenantRepository = tenantRepository;
        this.passwordHasher = passwordHasher;
        this.tenantAccessVerifier = tenantAccessVerifier;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse bootstrapAdmin(UUID tenantId, BootstrapAdminCommand command) {
        Tenant tenant = requireTenant(tenantId);

        if (appUserRepository.countByTenant(tenant) > 0) {
            throw new ConflictException("Bootstrap admin already exists for tenant %s.".formatted(tenantId));
        }

        AppUser user = createUser(tenant, command.fullName(), command.email(), command.password(), UserRole.ADMIN, UserStatus.ACTIVE);

        auditService.record(
                tenant,
                user.getId(),
                AuditAction.USER_BOOTSTRAPPED,
                "User",
                user.getId(),
                Map.of("email", user.getEmail(), "role", user.getRole().name())
        );

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse create(CreateUserCommand command) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN);

        Tenant tenant = requireTenant(tenantId);
        AppUser user = createUser(tenant, command.fullName(), command.email(), command.password(), command.role(), command.status());

        auditService.record(
                tenant,
                tenantAccessVerifier.currentUserId(),
                AuditAction.USER_CREATED,
                "User",
                user.getId(),
                Map.of("email", user.getEmail(), "role", user.getRole().name())
        );

        return UserResponse.from(user);
    }

    public List<UserResponse> list() {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);

        return appUserRepository.listByTenantId(tenantId).stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(UUID userId) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);

        return UserResponse.from(requireUser(tenantId, userId));
    }

    @Transactional
    public UserResponse updateStatus(UUID userId, UpdateUserStatusCommand command) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN);

        AppUser user = requireUser(tenantId, userId);
        user.updateStatus(command.status());

        auditService.record(
                user.getTenant(),
                tenantAccessVerifier.currentUserId(),
                AuditAction.USER_STATUS_UPDATED,
                "User",
                user.getId(),
                Map.of("status", user.getStatus().name())
        );

        return UserResponse.from(user);
    }

    private AppUser createUser(
            Tenant tenant,
            String fullName,
            String email,
            String password,
            UserRole role,
            UserStatus status
    ) {
        String normalizedEmail = AppUser.normalizeEmail(email);
        if (appUserRepository.existsByTenantIdAndEmail(tenant.getId(), normalizedEmail)) {
            throw new ConflictException("User email '%s' is already in use for tenant %s.".formatted(normalizedEmail, tenant.getSlug()));
        }

        AppUser user = AppUser.create(
                tenant,
                fullName,
                normalizedEmail,
                passwordHasher.hash(password),
                role,
                status
        );
        appUserRepository.persist(user);
        return user;
    }

    private Tenant requireTenant(UUID tenantId) {
        return tenantRepository.findByIdOptional(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant %s was not found.".formatted(tenantId)));
    }

    private AppUser requireUser(UUID tenantId, UUID userId) {
        return appUserRepository.findByTenantIdAndId(tenantId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User %s was not found.".formatted(userId)));
    }
}
