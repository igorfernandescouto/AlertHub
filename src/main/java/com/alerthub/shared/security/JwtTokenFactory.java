package com.alerthub.shared.security;

import java.time.Duration;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.alerthub.tenant.domain.Tenant;
import com.alerthub.user.domain.AppUser;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtTokenFactory {

    private final long tokenDurationHours;

    public JwtTokenFactory(@ConfigProperty(name = "alerthub.security.token-duration-hours") long tokenDurationHours) {
        this.tokenDurationHours = tokenDurationHours;
    }

    public String create(AppUser user, Tenant tenant) {
        return Jwt.issuer("alerthub")
                .subject(user.getId().toString())
                .upn(user.getEmail())
                .groups(Set.of(user.getRole().name()))
                .claim("userId", user.getId().toString())
                .claim("tenantId", tenant.getId().toString())
                .claim("tenantSlug", tenant.getSlug())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .expiresIn(Duration.ofHours(tokenDurationHours))
                .sign();
    }

    public long tokenDurationSeconds() {
        return Duration.ofHours(tokenDurationHours).toSeconds();
    }
}
