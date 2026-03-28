package com.alerthub.notification.domain;

import java.time.Instant;
import java.util.Objects;

import com.alerthub.event.domain.EventRecord;
import com.alerthub.rule.domain.AlertRule;
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
@Table(name = "notification_records")
public class NotificationRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_record_id", nullable = false, updatable = false)
    private EventRecord eventRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_rule_id", nullable = false, updatable = false)
    private AlertRule alertRule;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertPriority priority;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private NotificationStatus status;

    @Column(name = "error_message", length = 400)
    private String errorMessage;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    protected NotificationRecord() {
    }

    private NotificationRecord(
            Tenant tenant,
            EventRecord eventRecord,
            AlertRule alertRule,
            String eventType,
            NotificationChannel channel,
            AlertPriority priority,
            String title,
            String message
    ) {
        this.tenant = Objects.requireNonNull(tenant, "tenant is required");
        this.eventRecord = Objects.requireNonNull(eventRecord, "eventRecord is required");
        this.alertRule = Objects.requireNonNull(alertRule, "alertRule is required");
        this.eventType = Objects.requireNonNull(eventType, "eventType is required");
        this.channel = Objects.requireNonNull(channel, "channel is required");
        this.priority = Objects.requireNonNull(priority, "priority is required");
        this.title = Objects.requireNonNull(title, "title is required");
        this.message = Objects.requireNonNull(message, "message is required");
        this.status = NotificationStatus.PENDING;
    }

    public static NotificationRecord create(
            Tenant tenant,
            EventRecord eventRecord,
            AlertRule alertRule,
            String eventType,
            NotificationChannel channel,
            AlertPriority priority,
            String title,
            String message
    ) {
        return new NotificationRecord(tenant, eventRecord, alertRule, eventType, channel, priority, title, message);
    }

    public void markSent() {
        status = NotificationStatus.SENT;
        deliveredAt = Instant.now();
        errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public EventRecord getEventRecord() {
        return eventRecord;
    }

    public AlertRule getAlertRule() {
        return alertRule;
    }

    public String getEventType() {
        return eventType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public AlertPriority getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }
}
