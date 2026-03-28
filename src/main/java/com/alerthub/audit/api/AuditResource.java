package com.alerthub.audit.api;

import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.alerthub.audit.api.dto.AuditEntryResponse;
import com.alerthub.audit.application.AuditService;
import com.alerthub.audit.domain.AuditAction;
import com.alerthub.shared.paging.PageQuery;
import com.alerthub.shared.paging.PagedResponse;
import com.alerthub.shared.security.TenantAccessVerifier;
import com.alerthub.user.domain.UserRole;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/audits")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Audit")
public class AuditResource {

    private final AuditService auditService;
    private final TenantAccessVerifier tenantAccessVerifier;

    public AuditResource(AuditService auditService, TenantAccessVerifier tenantAccessVerifier) {
        this.auditService = auditService;
        this.tenantAccessVerifier = tenantAccessVerifier;
    }

    @GET
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "Search audit trail entries for the current tenant.")
    public PagedResponse<AuditEntryResponse> search(
            @QueryParam("action") AuditAction action,
            @QueryParam("from") Instant from,
            @QueryParam("to") Instant to,
            @BeanParam PageQuery pageQuery
    ) {
        var tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);
        return auditService.search(tenantId, action, from, to, pageQuery.page(), pageQuery.size());
    }
}
