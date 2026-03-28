package com.alerthub.event.api.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IngestEventRequest(
        @NotBlank @Size(max = 80) String eventType,
        @NotBlank @Size(max = 120) String source,
        @NotNull JsonNode payload,
        @NotNull Instant occurredAt,
        @NotBlank @Size(max = 120) String idempotencyKey
) {
}
