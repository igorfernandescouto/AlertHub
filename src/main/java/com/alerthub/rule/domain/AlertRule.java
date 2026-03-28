package com.alerthub.rule.domain;

import java.util.Objects;

import com.alerthub.notification.domain.AlertPriority;
import com.alerthub.notification.domain.NotificationChannel;
import com.alerthub.shared.domain.BaseEntity;
import com.alerthub.tenant.domain.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "alert_rules")
public class AlertRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "payload_field", length = 160)
    private String payloadField;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleOperator operator;

    @Column(name = "expected_value", length = 200)
    private String expectedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannel channel;

    @Column(name = "message_template", length = 300)
    private String messageTemplate;

    @Column(nullable = false)
    private boolean active;

    protected AlertRule() {
    }

    private AlertRule(
            Tenant tenant,
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
        this.tenant = Objects.requireNonNull(tenant, "tenant is required");
        this.name = requireValue(name, "name");
        this.eventType = requireValue(eventType, "eventType");
        this.payloadField = blankToNull(payloadField);
        this.operator = Objects.requireNonNull(operator, "operator is required");
        this.expectedValue = blankToNull(expectedValue);
        this.priority = Objects.requireNonNull(priority, "priority is required");
        this.channel = Objects.requireNonNull(channel, "channel is required");
        this.messageTemplate = blankToNull(messageTemplate);
        this.active = active;
    }

    public static AlertRule create(
            Tenant tenant,
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
        return new AlertRule(tenant, name, eventType, payloadField, operator, expectedValue, priority, channel, messageTemplate, active);
    }

    public void update(
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
        this.name = requireValue(name, "name");
        this.eventType = requireValue(eventType, "eventType");
        this.payloadField = blankToNull(payloadField);
        this.operator = Objects.requireNonNull(operator, "operator is required");
        this.expectedValue = blankToNull(expectedValue);
        this.priority = Objects.requireNonNull(priority, "priority is required");
        this.channel = Objects.requireNonNull(channel, "channel is required");
        this.messageTemplate = blankToNull(messageTemplate);
        this.active = active;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayloadField() {
        return payloadField;
    }

    public RuleOperator getOperator() {
        return operator;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public AlertPriority getPriority() {
        return priority;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public boolean isActive() {
        return active;
    }

    private static String requireValue(String value, String fieldName) {
        String sanitized = value == null ? "" : value.trim();
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return sanitized;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = value.trim();
        return sanitized.isBlank() ? null : sanitized;
    }
}
