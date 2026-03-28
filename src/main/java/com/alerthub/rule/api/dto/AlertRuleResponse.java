package com.alerthub.rule.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.alerthub.notification.domain.AlertPriority;
import com.alerthub.notification.domain.NotificationChannel;
import com.alerthub.rule.domain.AlertRule;
import com.alerthub.rule.domain.RuleOperator;

public record AlertRuleResponse(
        UUID id,
        UUID tenantId,
        String name,
        String eventType,
        String payloadField,
        RuleOperator operator,
        String expectedValue,
        AlertPriority priority,
        NotificationChannel channel,
        String messageTemplate,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static AlertRuleResponse from(AlertRule rule) {
        return new AlertRuleResponse(
                rule.getId(),
                rule.getTenant().getId(),
                rule.getName(),
                rule.getEventType(),
                rule.getPayloadField(),
                rule.getOperator(),
                rule.getExpectedValue(),
                rule.getPriority(),
                rule.getChannel(),
                rule.getMessageTemplate(),
                rule.isActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
