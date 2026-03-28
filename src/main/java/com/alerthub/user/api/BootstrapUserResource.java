package com.alerthub.user.api;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.alerthub.user.api.dto.BootstrapAdminRequest;
import com.alerthub.user.api.dto.UserResponse;
import com.alerthub.user.application.BootstrapAdminCommand;
import com.alerthub.user.application.UserService;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/tenants/{tenantId}/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Users")
public class BootstrapUserResource {

    private final UserService userService;

    public BootstrapUserResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    @Path("/bootstrap-admin")
    @PermitAll
    @Operation(summary = "Create the first admin user for a tenant.")
    public UserResponse bootstrapAdmin(@PathParam("tenantId") UUID tenantId, @Valid BootstrapAdminRequest request) {
        return userService.bootstrapAdmin(tenantId, new BootstrapAdminCommand(
                request.fullName(),
                request.email(),
                request.password()
        ));
    }
}
