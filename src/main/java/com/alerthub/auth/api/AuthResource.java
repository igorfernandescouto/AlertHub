package com.alerthub.auth.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.alerthub.auth.api.dto.AuthTokenResponse;
import com.alerthub.auth.api.dto.LoginRequest;
import com.alerthub.auth.application.AuthService;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication")
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Authenticate a tenant user and return a JWT.")
    public AuthTokenResponse login(@Valid LoginRequest request) {
        return authService.login(request.tenantSlug(), request.email(), request.password());
    }
}
