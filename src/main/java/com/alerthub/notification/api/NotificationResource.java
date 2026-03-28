package com.alerthub.notification.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.alerthub.notification.api.dto.NotificationResponse;
import com.alerthub.notification.application.NotificationService;
import com.alerthub.notification.domain.NotificationChannel;
import com.alerthub.notification.domain.NotificationStatus;
import com.alerthub.shared.paging.PageQuery;
import com.alerthub.shared.paging.PagedResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
public class NotificationResource {

    private final NotificationService notificationService;

    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GET
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "Search notification history for the current tenant.")
    public PagedResponse<NotificationResponse> search(
            @QueryParam("status") NotificationStatus status,
            @QueryParam("channel") NotificationChannel channel,
            @BeanParam PageQuery pageQuery
    ) {
        return notificationService.search(status, channel, pageQuery.page(), pageQuery.size());
    }
}
