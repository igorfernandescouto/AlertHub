package com.alerthub.rule.application;

import com.alerthub.notification.domain.AlertPriority;
import com.alerthub.notification.domain.NotificationChannel;
import com.alerthub.rule.domain.RuleOperator;

public record UpdateAlertRuleCommand(
        String name,
        String eventType,
        String payloadField,
        RuleOperator operator,
        String expectedValue,
        AlertPriority priority,
        NotificationChannel channel,
        String messageTemplate,
        boolean active
) {
}
