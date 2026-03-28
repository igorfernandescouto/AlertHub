package com.alerthub.shared.api;

import java.util.List;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(NotAuthorizedException exception) {
        ApiError error = ApiError.of(
                Response.Status.UNAUTHORIZED.getStatusCode(),
                Response.Status.UNAUTHORIZED.getReasonPhrase(),
                "Authentication is required.",
                uriInfo.getPath(),
                List.of()
        );

        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
