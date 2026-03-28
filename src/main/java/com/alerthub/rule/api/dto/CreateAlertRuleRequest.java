package com.alerthub.rule.api.dto;

import com.alerthub.notification.domain.AlertPriority;
import com.alerthub.notification.domain.NotificationChannel;
import com.alerthub.rule.domain.RuleOperator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAlertRuleRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 80) String eventType,
        @Size(max = 160) String payloadField,
        @NotNull RuleOperator operator,
        @Size(max = 200) String expectedValue,
        @NotNull AlertPriority priority,
        @NotNull NotificationChannel channel,
        @Size(max = 300) String messageTemplate,
        @NotNull Boolean active
) {
}
