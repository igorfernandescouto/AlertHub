package com.alerthub.user.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.alerthub.tenant.domain.Tenant;
import com.alerthub.user.domain.AppUser;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppUserRepository implements PanacheRepositoryBase<AppUser, UUID> {

    public Optional<AppUser> findByTenantAndEmail(Tenant tenant, String email) {
        return find("tenant = ?1 and email = ?2", tenant, email).firstResultOptional();
    }

    public Optional<AppUser> findByTenantIdAndId(UUID tenantId, UUID userId) {
        return find("tenant.id = ?1 and id = ?2", tenantId, userId).firstResultOptional();
    }

    public List<AppUser> listByTenantId(UUID tenantId) {
        return list("tenant.id", Sort.by("fullName"), tenantId);
    }

    public long countByTenant(Tenant tenant) {
        return count("tenant", tenant);
    }

    public boolean existsByTenantIdAndEmail(UUID tenantId, String email) {
        return count("tenant.id = ?1 and email = ?2", tenantId, email) > 0;
    }
}
