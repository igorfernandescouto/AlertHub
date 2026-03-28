package com.alerthub.tenant.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.alerthub.tenant.domain.Tenant;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TenantRepository implements PanacheRepositoryBase<Tenant, UUID> {

    public boolean existsBySlug(String slug) {
        return count("slug", slug) > 0;
    }

    public Optional<Tenant> findBySlugOptional(String slug) {
        return find("slug", slug).firstResultOptional();
    }

    public List<Tenant> listAllOrdered() {
        return listAll(Sort.by("name").and("createdAt"));
    }
}
