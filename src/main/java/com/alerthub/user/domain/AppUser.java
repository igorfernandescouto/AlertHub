package com.alerthub.user.domain;

import java.util.Locale;
import java.util.Objects;

import com.alerthub.shared.domain.BaseEntity;
import com.alerthub.tenant.domain.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class AppUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserStatus status;

    protected AppUser() {
    }

    private AppUser(Tenant tenant, String fullName, String email, String passwordHash, UserRole role, UserStatus status) {
        this.tenant = Objects.requireNonNull(tenant, "tenant is required");
        this.fullName = sanitizeName(fullName);
        this.email = normalizeEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "password hash is required");
        this.role = Objects.requireNonNull(role, "role is required");
        this.status = Objects.requireNonNull(status, "status is required");
    }

    public static AppUser create(
            Tenant tenant,
            String fullName,
            String email,
            String passwordHash,
            UserRole role,
            UserStatus status
    ) {
        return new AppUser(tenant, fullName, email, passwordHash, role, status);
    }

    public void updateStatus(UserStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    private static String sanitizeName(String value) {
        String sanitized = value == null ? "" : value.trim();
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
        return sanitized;
    }

    public static String normalizeEmail(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        return normalized;
    }
}
