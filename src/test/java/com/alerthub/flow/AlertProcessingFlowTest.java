package com.alerthub.flow;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.alerthub.support.ApiTestSupport;
import com.alerthub.support.InMemoryMessagingTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;

@QuarkusTest
@QuarkusTestResource(InMemoryMessagingTestResource.class)
class AlertProcessingFlowTest {

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector inMemoryConnector;

    @Test
    void shouldCreateRuleIngestEventAndExposeHistory() {
        var session = ApiTestSupport.bootstrapTenantAndLogin();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", session.authorizationHeader())
                .body(Map.of(
                        "name", "High order failure",
                        "eventType", "ORDER_FAILED",
                        "payloadField", "severity",
                        "operator", "EQUALS",
                        "expectedValue", "HIGH",
                        "priority", "CRITICAL",
                        "channel", "INTERNAL",
                        "messageTemplate", "Order failure with level {{value}} from {{source}}",
                        "active", true
                ))
                .when().post("/api/v1/rules")
                .then()
                .statusCode(200)
                .body("eventType", equalTo("ORDER_FAILED"));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", session.authorizationHeader())
                .body(Map.of(
                        "eventType", "ORDER_FAILED",
                        "source", "order-service",
                        "payload", Map.of("severity", "HIGH", "orderId", "ORD-123"),
                        "occurredAt", Instant.now().toString(),
                        "idempotencyKey", "evt-" + session.tenantSlug()
                ))
                .when().post("/api/v1/events")
                .then()
                .statusCode(200)
                .body("status", equalTo("RECEIVED"));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var messages = inMemoryConnector.sink("events-out").received();
            org.junit.jupiter.api.Assertions.assertFalse(messages.isEmpty());
            String eventId = (String) messages.getFirst().getPayload();
            inMemoryConnector.source("events-in").send(eventId);
        });

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> given()
                .header("Authorization", session.authorizationHeader())
                .when().get("/api/v1/notifications")
                .then()
                .statusCode(200)
                .body("items.size()", equalTo(1))
                .body("items[0].status", equalTo("SENT"))
                .body("items[0].channel", equalTo("INTERNAL")));

        given()
                .header("Authorization", session.authorizationHeader())
                .when().get("/api/v1/audits")
                .then()
                .statusCode(200)
                .body("items.action", hasItems("EVENT_RECEIVED", "EVENT_PROCESSED", "NOTIFICATION_CREATED"));
    }
}
