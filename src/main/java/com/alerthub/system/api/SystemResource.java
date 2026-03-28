package com.alerthub.system.api;

import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/system")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "System")
public class SystemResource {

    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    @ConfigProperty(name = "quarkus.application.version")
    String applicationVersion;

    @GET
    @Path("/status")
    @Operation(summary = "Return application metadata and server time.")
    public SystemStatusResponse status() {
        return new SystemStatusResponse(applicationName, applicationVersion, Instant.now());
    }
}
