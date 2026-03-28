package com.alerthub.tenant.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.alerthub.shared.api.ApiError;
import com.alerthub.tenant.api.dto.CreateTenantRequest;
import com.alerthub.tenant.api.dto.TenantResponse;
import com.alerthub.tenant.api.dto.UpdateTenantStatusRequest;
import com.alerthub.tenant.application.CreateTenantCommand;
import com.alerthub.tenant.application.TenantService;
import com.alerthub.tenant.application.UpdateTenantStatusCommand;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/api/v1/tenants")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Tenants")
public class TenantResource {

    private final TenantService tenantService;

    public TenantResource(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @POST
    @PermitAll
    @Operation(summary = "Create a new tenant.")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Tenant created."),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation failure.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Slug already exists.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response create(@Valid CreateTenantRequest request, @Context UriInfo uriInfo) {
        TenantResponse response = tenantService.create(new CreateTenantCommand(
                request.name(),
                request.slug(),
                request.status()
        ));

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(response.id().toString())
                .build();

        return Response.created(location)
                .entity(response)
                .build();
    }

    @GET
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "List all tenants.")
    @APIResponse(
            responseCode = "200",
            description = "Ordered tenant list.",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = TenantResponse.class))
    )
    public List<TenantResponse> list() {
        return tenantService.list();
    }

    @GET
    @Path("/{tenantId}")
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "Get a tenant by id.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Tenant found."),
            @APIResponse(
                    responseCode = "404",
                    description = "Tenant not found.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TenantResponse findById(@PathParam("tenantId") UUID tenantId) {
        return tenantService.findById(tenantId);
    }

    @PATCH
    @Path("/{tenantId}/status")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Update tenant status.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Tenant status updated."),
            @APIResponse(
                    responseCode = "400",
                    description = "Validation failure.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Tenant not found.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TenantResponse updateStatus(@PathParam("tenantId") UUID tenantId, @Valid UpdateTenantStatusRequest request) {
        return tenantService.updateStatus(tenantId, new UpdateTenantStatusCommand(request.status()));
    }
}
