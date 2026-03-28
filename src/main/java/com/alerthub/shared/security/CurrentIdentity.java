package com.alerthub.shared.security;

import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.alerthub.user.domain.UserRole;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CurrentIdentity {

    private final SecurityIdentity securityIdentity;
    private final JsonWebToken jsonWebToken;

    public CurrentIdentity(SecurityIdentity securityIdentity, JsonWebToken jsonWebToken) {
        this.securityIdentity = securityIdentity;
        this.jsonWebToken = jsonWebToken;
    }

    public boolean isAnonymous() {
        return securityIdentity.isAnonymous();
    }

    public UUID userId() {
        return UUID.fromString(jsonWebToken.getClaim("userId"));
    }

    public UUID tenantId() {
        return UUID.fromString(jsonWebToken.getClaim("tenantId"));
    }

    public String email() {
        return jsonWebToken.getClaim("email");
    }

    public UserRole role() {
        return securityIdentity.getRoles().stream()
                .map(UserRole::valueOf)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no role."));
    }

    public Set<String> roles() {
        return securityIdentity.getRoles();
    }
}
