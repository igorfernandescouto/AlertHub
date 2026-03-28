package com.alerthub.event.api;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.alerthub.event.api.dto.EventResponse;
import com.alerthub.event.api.dto.IngestEventRequest;
import com.alerthub.event.application.CreateEventCommand;
import com.alerthub.event.application.EventIngestionService;
import com.alerthub.shared.paging.PageQuery;
import com.alerthub.shared.paging.PagedResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Events")
public class EventResource {

    private final EventIngestionService eventIngestionService;

    public EventResource(EventIngestionService eventIngestionService) {
        this.eventIngestionService = eventIngestionService;
    }

    @POST
    @RolesAllowed({ "ADMIN", "OPERATOR" })
    @Operation(summary = "Ingest an event for the current tenant.")
    public EventResponse ingest(@Valid IngestEventRequest request) {
        return eventIngestionService.ingest(new CreateEventCommand(
                request.eventType(),
                request.source(),
                request.payload(),
                request.occurredAt(),
                request.idempotencyKey()
        ));
    }

    @POST
    @Path("/{eventId}/reprocess")
    @RolesAllowed({ "ADMIN", "OPERATOR" })
    @Operation(summary = "Requeue an event for processing.")
    public EventResponse reprocess(@PathParam("eventId") UUID eventId) {
        return eventIngestionService.reprocess(eventId);
    }

    @GET
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "List ingested events from the current tenant.")
    public PagedResponse<EventResponse> list(@BeanParam PageQuery pageQuery) {
        return eventIngestionService.list(pageQuery.page(), pageQuery.size());
    }
}
