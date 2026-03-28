package com.alerthub.rule.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alerthub.audit.application.AuditService;
import com.alerthub.audit.domain.AuditAction;
import com.alerthub.rule.api.dto.AlertRuleResponse;
import com.alerthub.rule.domain.AlertRule;
import com.alerthub.rule.infrastructure.AlertRuleRepository;
import com.alerthub.shared.exception.ResourceNotFoundException;
import com.alerthub.shared.security.TenantAccessVerifier;
import com.alerthub.tenant.domain.Tenant;
import com.alerthub.tenant.infrastructure.TenantRepository;
import com.alerthub.user.domain.UserRole;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final TenantRepository tenantRepository;
    private final TenantAccessVerifier tenantAccessVerifier;
    private final AuditService auditService;

    public AlertRuleService(
            AlertRuleRepository alertRuleRepository,
            TenantRepository tenantRepository,
            TenantAccessVerifier tenantAccessVerifier,
            AuditService auditService
    ) {
        this.alertRuleRepository = alertRuleRepository;
        this.tenantRepository = tenantRepository;
        this.tenantAccessVerifier = tenantAccessVerifier;
        this.auditService = auditService;
    }

    @Transactional
    public AlertRuleResponse create(CreateAlertRuleCommand command) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR);

        Tenant tenant = requireTenant(tenantId);
        validate(command.operator(), command.payloadField(), command.expectedValue());
        AlertRule rule = AlertRule.create(
                tenant,
                command.name(),
                command.eventType(),
                command.payloadField(),
                command.operator(),
                command.expectedValue(),
                command.priority(),
                command.channel(),
                command.messageTemplate(),
                command.active()
        );
        alertRuleRepository.persist(rule);

        auditService.record(
                tenant,
                tenantAccessVerifier.currentUserId(),
                AuditAction.RULE_CREATED,
                "AlertRule",
                rule.getId(),
                Map.of("eventType", rule.getEventType(), "channel", rule.getChannel().name())
        );

        return AlertRuleResponse.from(rule);
    }

    public List<AlertRuleResponse> list() {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);

        return alertRuleRepository.findByTenantId(tenantId, 0, 200).list().stream()
                .map(AlertRuleResponse::from)
                .toList();
    }

    public AlertRuleResponse findById(UUID ruleId) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);

        return AlertRuleResponse.from(requireRule(tenantId, ruleId));
    }

    @Transactional
    public AlertRuleResponse update(UUID ruleId, UpdateAlertRuleCommand command) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR);

        AlertRule rule = requireRule(tenantId, ruleId);
        validate(command.operator(), command.payloadField(), command.expectedValue());
        rule.update(
                command.name(),
                command.eventType(),
                command.payloadField(),
                command.operator(),
                command.expectedValue(),
                command.priority(),
                command.channel(),
                command.messageTemplate(),
                command.active()
        );

        auditService.record(
                rule.getTenant(),
                tenantAccessVerifier.currentUserId(),
                AuditAction.RULE_UPDATED,
                "AlertRule",
                rule.getId(),
                Map.of("eventType", rule.getEventType(), "active", rule.isActive())
        );

        return AlertRuleResponse.from(rule);
    }

    @Transactional
    public void delete(UUID ruleId) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR);

        AlertRule rule = requireRule(tenantId, ruleId);
        alertRuleRepository.delete(rule);

        auditService.record(
                rule.getTenant(),
                tenantAccessVerifier.currentUserId(),
                AuditAction.RULE_DELETED,
                "AlertRule",
                rule.getId(),
                Map.of("name", rule.getName())
        );
    }

    private Tenant requireTenant(UUID tenantId) {
        return tenantRepository.findByIdOptional(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant %s was not found.".formatted(tenantId)));
    }

    private AlertRule requireRule(UUID tenantId, UUID ruleId) {
        return alertRuleRepository.findByTenantIdAndId(tenantId, ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert rule %s was not found.".formatted(ruleId)));
    }

    private void validate(com.alerthub.rule.domain.RuleOperator operator, String payloadField, String expectedValue) {
        if (operator == com.alerthub.rule.domain.RuleOperator.ANY) {
            return;
        }
        if (payloadField != null && !payloadField.isBlank() && expectedValue != null && !expectedValue.isBlank()) {
            return;
        }
        throw new IllegalArgumentException("payloadField and expectedValue are required when operator is not ANY");
    }
}
