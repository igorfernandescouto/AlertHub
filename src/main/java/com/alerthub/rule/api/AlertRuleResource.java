package com.alerthub.rule.api;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.alerthub.rule.api.dto.AlertRuleResponse;
import com.alerthub.rule.api.dto.CreateAlertRuleRequest;
import com.alerthub.rule.api.dto.UpdateAlertRuleRequest;
import com.alerthub.rule.application.AlertRuleService;
import com.alerthub.rule.application.CreateAlertRuleCommand;
import com.alerthub.rule.application.UpdateAlertRuleCommand;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/rules")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Alert Rules")
public class AlertRuleResource {

    private final AlertRuleService alertRuleService;

    public AlertRuleResource(AlertRuleService alertRuleService) {
        this.alertRuleService = alertRuleService;
    }

    @POST
    @RolesAllowed({ "ADMIN", "OPERATOR" })
    @Operation(summary = "Create an alert rule for the current tenant.")
    public AlertRuleResponse create(@Valid CreateAlertRuleRequest request) {
        return alertRuleService.create(new CreateAlertRuleCommand(
                request.name(),
                request.eventType(),
                request.payloadField(),
                request.operator(),
                request.expectedValue(),
                request.priority(),
                request.channel(),
                request.messageTemplate(),
                request.active()
        ));
    }

    @GET
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "List alert rules from the current tenant.")
    public List<AlertRuleResponse> list() {
        return alertRuleService.list();
    }

    @GET
    @Path("/{ruleId}")
    @RolesAllowed({ "ADMIN", "OPERATOR", "VIEWER" })
    @Operation(summary = "Get one alert rule from the current tenant.")
    public AlertRuleResponse findById(@PathParam("ruleId") UUID ruleId) {
        return alertRuleService.findById(ruleId);
    }

    @PUT
    @Path("/{ruleId}")
    @RolesAllowed({ "ADMIN", "OPERATOR" })
    @Operation(summary = "Update an alert rule from the current tenant.")
    public AlertRuleResponse update(@PathParam("ruleId") UUID ruleId, @Valid UpdateAlertRuleRequest request) {
        return alertRuleService.update(ruleId, new UpdateAlertRuleCommand(
                request.name(),
                request.eventType(),
                request.payloadField(),
                request.operator(),
                request.expectedValue(),
                request.priority(),
                request.channel(),
                request.messageTemplate(),
                request.active()
        ));
    }

    @DELETE
    @Path("/{ruleId}")
    @RolesAllowed({ "ADMIN", "OPERATOR" })
    @Operation(summary = "Delete an alert rule from the current tenant.")
    public void delete(@PathParam("ruleId") UUID ruleId) {
        alertRuleService.delete(ruleId);
    }
}
