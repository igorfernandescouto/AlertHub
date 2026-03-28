package com.alerthub.event.application;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.alerthub.audit.application.AuditService;
import com.alerthub.audit.domain.AuditAction;
import com.alerthub.event.domain.EventRecord;
import com.alerthub.event.infrastructure.EventRecordRepository;
import com.alerthub.notification.domain.NotificationRecord;
import com.alerthub.notification.infrastructure.NotificationRecordRepository;
import com.alerthub.rule.domain.AlertRule;
import com.alerthub.rule.domain.RuleOperator;
import com.alerthub.rule.infrastructure.AlertRuleRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EventProcessingService {

    private final EventRecordRepository eventRecordRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final NotificationRecordRepository notificationRecordRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final JsonFieldExtractor jsonFieldExtractor;
    private final EventPublisher eventPublisher;
    private final int maxRetries;

    public EventProcessingService(
            EventRecordRepository eventRecordRepository,
            AlertRuleRepository alertRuleRepository,
            NotificationRecordRepository notificationRecordRepository,
            AuditService auditService,
            ObjectMapper objectMapper,
            JsonFieldExtractor jsonFieldExtractor,
            EventPublisher eventPublisher,
            @ConfigProperty(name = "alerthub.processing.max-retries") int maxRetries
    ) {
        this.eventRecordRepository = eventRecordRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.notificationRecordRepository = notificationRecordRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.jsonFieldExtractor = jsonFieldExtractor;
        this.eventPublisher = eventPublisher;
        this.maxRetries = maxRetries;
    }

    @Transactional
    public void process(UUID eventId) {
        EventRecord event = eventRecordRepository.findByIdOptional(eventId).orElse(null);
        if (event == null || event.isProcessed()) {
            return;
        }

        try {
            event.markProcessing();
            JsonNode payload = objectMapper.readTree(event.getPayload());
            List<AlertRule> rules = alertRuleRepository.listActiveByTenantIdAndEventType(event.getTenant().getId(), event.getEventType());
            long matchedRules = createNotifications(event, payload, rules);
            event.markProcessed();

            auditService.record(
                    event.getTenant(),
                    null,
                    AuditAction.EVENT_PROCESSED,
                    "EventRecord",
                    event.getId(),
                    Map.of("matchedRules", matchedRules, "attempts", event.getProcessingAttempts())
            );
        } catch (Exception exception) {
            handleFailure(event, exception);
        }
    }

    private long createNotifications(EventRecord event, JsonNode payload, List<AlertRule> rules) {
        return rules.stream()
                .filter(rule -> matches(rule, payload))
                .map(rule -> createNotification(event, rule, payload))
                .count();
    }

    private NotificationRecord createNotification(EventRecord event, AlertRule rule, JsonNode payload) {
        String title = "%s %s".formatted(rule.getPriority().name(), event.getEventType());
        String message = renderMessage(rule, event, payload);

        NotificationRecord notification = NotificationRecord.create(
                event.getTenant(),
                event,
                rule,
                event.getEventType(),
                rule.getChannel(),
                rule.getPriority(),
                title,
                message
        );
        notificationRecordRepository.persist(notification);
        notification.markSent();

        auditService.record(
                event.getTenant(),
                null,
                AuditAction.NOTIFICATION_CREATED,
                "NotificationRecord",
                notification.getId(),
                Map.of("channel", notification.getChannel().name(), "ruleId", rule.getId().toString())
        );

        return notification;
    }

    private boolean matches(AlertRule rule, JsonNode payload) {
        if (rule.getOperator() == RuleOperator.ANY) {
            return true;
        }

        JsonNode actualValue = jsonFieldExtractor.read(payload, rule.getPayloadField());
        return rule.getOperator().matches(actualValue, rule.getExpectedValue());
    }

    private String renderMessage(AlertRule rule, EventRecord event, JsonNode payload) {
        String template = rule.getMessageTemplate();
        if (template == null || template.isBlank()) {
            return "Rule '%s' matched event '%s' from source '%s'.".formatted(rule.getName(), event.getEventType(), event.getSource());
        }

        JsonNode actualValue = jsonFieldExtractor.read(payload, rule.getPayloadField());
        String value = actualValue == null ? "" : actualValue.asText();

        return template
                .replace("{{eventType}}", event.getEventType())
                .replace("{{source}}", event.getSource())
                .replace("{{tenantSlug}}", event.getTenant().getSlug())
                .replace("{{payloadField}}", rule.getPayloadField() == null ? "" : rule.getPayloadField())
                .replace("{{value}}", value);
    }

    private void handleFailure(EventRecord event, Exception exception) {
        event.markFailed(exception.getMessage());
        boolean shouldRetry = event.getProcessingAttempts() < maxRetries;

        auditService.record(
                event.getTenant(),
                null,
                AuditAction.EVENT_FAILED,
                "EventRecord",
                event.getId(),
                Map.of(
                        "attempts", event.getProcessingAttempts(),
                        "maxRetries", maxRetries,
                        "error", exception.getMessage(),
                        "retryScheduled", shouldRetry
                )
        );

        if (shouldRetry) {
            eventPublisher.publish(event.getId());
        }
    }
}
