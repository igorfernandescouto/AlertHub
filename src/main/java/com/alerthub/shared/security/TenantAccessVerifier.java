package com.alerthub.shared.security;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import com.alerthub.user.domain.UserRole;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;

@ApplicationScoped
public class TenantAccessVerifier {

    private final CurrentIdentity currentIdentity;

    public TenantAccessVerifier(CurrentIdentity currentIdentity) {
        this.currentIdentity = currentIdentity;
    }

    public UUID currentTenantId() {
        ensureAuthenticated();
        return currentIdentity.tenantId();
    }

    public UUID currentUserId() {
        ensureAuthenticated();
        return currentIdentity.userId();
    }

    public void requireTenantAccess(UUID tenantId) {
        ensureAuthenticated();
        if (tenantId.equals(currentIdentity.tenantId())) {
            return;
        }
        throw new ForbiddenException();
    }

    public void requireAnyRole(UserRole... roles) {
        ensureAuthenticated();
        Set<String> currentRoles = currentIdentity.roles();
        boolean allowed = Arrays.stream(roles)
                .map(Enum::name)
                .anyMatch(currentRoles::contains);

        if (allowed) {
            return;
        }

        throw new ForbiddenException();
    }

    public void requireTenantRole(UUID tenantId, UserRole... roles) {
        requireTenantAccess(tenantId);
        requireAnyRole(roles);
    }

    private void ensureAuthenticated() {
        if (!currentIdentity.isAnonymous()) {
            return;
        }
        throw new NotAuthorizedException("Bearer");
    }
}
