package com.alerthub.notification.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.alerthub.notification.domain.AlertPriority;
import com.alerthub.notification.domain.NotificationChannel;
import com.alerthub.notification.domain.NotificationRecord;
import com.alerthub.notification.domain.NotificationStatus;

public record NotificationResponse(
        UUID id,
        UUID tenantId,
        UUID eventId,
        UUID alertRuleId,
        String eventType,
        NotificationChannel channel,
        AlertPriority priority,
        String title,
        String message,
        NotificationStatus status,
        String errorMessage,
        Instant deliveredAt,
        Instant createdAt
) {

    public static NotificationResponse from(NotificationRecord notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTenant().getId(),
                notification.getEventRecord().getId(),
                notification.getAlertRule().getId(),
                notification.getEventType(),
                notification.getChannel(),
                notification.getPriority(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getErrorMessage(),
                notification.getDeliveredAt(),
                notification.getCreatedAt()
        );
    }
}
