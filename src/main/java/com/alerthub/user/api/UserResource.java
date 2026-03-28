package com.alerthub.user.api;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.alerthub.user.api.dto.BootstrapAdminRequest;
import com.alerthub.user.api.dto.CreateUserRequest;
import com.alerthub.user.api.dto.UpdateUserStatusRequest;
import com.alerthub.user.api.dto.UserResponse;
import com.alerthub.user.application.BootstrapAdminCommand;
import com.alerthub.user.application.CreateUserCommand;
import com.alerthub.user.application.UpdateUserStatusCommand;
import com.alerthub.user.application.UserService;

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
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Users")
public class UserResource {

    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Create a user for the current tenant.")
    public UserResponse create(@Valid CreateUserRequest request) {
        return userService.create(new CreateUserCommand(
                request.fullName(),
                request.email(),
                request.password(),
                request.role(),
                request.status()
        ));
    }

    @GET
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "List users from the current tenant.")
    public List<UserResponse> list() {
        return userService.list();
    }

    @GET
    @Path("/{userId}")
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "Get a user from the current tenant.")
    public UserResponse findById(@PathParam("userId") UUID userId) {
        return userService.findById(userId);
    }

    @PATCH
    @Path("/{userId}/status")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Update a user status in the current tenant.")
    public UserResponse updateStatus(@PathParam("userId") UUID userId, @Valid UpdateUserStatusRequest request) {
        return userService.updateStatus(userId, new UpdateUserStatusCommand(request.status()));
    }
}
