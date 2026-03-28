package com.alerthub.tenant.domain;

import java.util.Locale;
import java.util.Objects;

import com.alerthub.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TenantStatus status;

    protected Tenant() {
    }

    private Tenant(String name, String slug, TenantStatus status) {
        this.name = sanitizeName(name);
        this.slug = sanitizeSlug(slug);
        this.status = Objects.requireNonNull(status, "status is required");
    }

    public static Tenant create(String name, String slug, TenantStatus status) {
        return new Tenant(name, slug, status);
    }

    public void updateStatus(TenantStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public TenantStatus getStatus() {
        return status;
    }

    private static String sanitizeName(String value) {
        String sanitized = value == null ? "" : value.trim();
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        return sanitized;
    }

    private static String sanitizeSlug(String value) {
        String sanitized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("slug is required");
        }
        return sanitized;
    }
}
