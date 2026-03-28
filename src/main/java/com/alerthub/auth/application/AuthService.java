package com.alerthub.auth.application;

import java.util.Map;

import com.alerthub.audit.application.AuditService;
import com.alerthub.audit.domain.AuditAction;
import com.alerthub.auth.api.dto.AuthTokenResponse;
import com.alerthub.shared.exception.ResourceNotFoundException;
import com.alerthub.shared.security.JwtTokenFactory;
import com.alerthub.shared.security.PasswordHasher;
import com.alerthub.tenant.domain.Tenant;
import com.alerthub.tenant.domain.TenantStatus;
import com.alerthub.tenant.infrastructure.TenantRepository;
import com.alerthub.user.domain.AppUser;
import com.alerthub.user.domain.UserStatus;
import com.alerthub.user.infrastructure.AppUserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;

@ApplicationScoped
public class AuthService {

    private final TenantRepository tenantRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenFactory jwtTokenFactory;
    private final AuditService auditService;

    public AuthService(
            TenantRepository tenantRepository,
            AppUserRepository appUserRepository,
            PasswordHasher passwordHasher,
            JwtTokenFactory jwtTokenFactory,
            AuditService auditService
    ) {
        this.tenantRepository = tenantRepository;
        this.appUserRepository = appUserRepository;
        this.passwordHasher = passwordHasher;
        this.jwtTokenFactory = jwtTokenFactory;
        this.auditService = auditService;
    }

    @Transactional
    public AuthTokenResponse login(String tenantSlug, String email, String password) {
        Tenant tenant = tenantRepository.findBySlugOptional(tenantSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant slug '%s' was not found.".formatted(tenantSlug)));

        requireActiveTenant(tenant);

        AppUser user = appUserRepository.findByTenantAndEmail(tenant, AppUser.normalizeEmail(email))
                .orElseThrow(() -> new NotAuthorizedException("Bearer"));

        requireActiveUser(user);
        validatePassword(password, user.getPasswordHash());

        auditService.record(
                tenant,
                user.getId(),
                AuditAction.USER_LOGGED_IN,
                "User",
                user.getId(),
                Map.of("email", user.getEmail())
        );

        return new AuthTokenResponse(
                jwtTokenFactory.create(user, tenant),
                "Bearer",
                jwtTokenFactory.tokenDurationSeconds()
        );
    }

    private void requireActiveTenant(Tenant tenant) {
        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            return;
        }
        throw new NotAuthorizedException("Bearer");
    }

    private void requireActiveUser(AppUser user) {
        if (user.getStatus() == UserStatus.ACTIVE) {
            return;
        }
        throw new NotAuthorizedException("Bearer");
    }

    private void validatePassword(String password, String passwordHash) {
        if (passwordHasher.matches(password, passwordHash)) {
            return;
        }
        throw new NotAuthorizedException("Bearer");
    }
}
