package com.alerthub.event.application;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JsonFieldExtractor {

    public JsonNode read(JsonNode payload, String path) {
        if (payload == null || path == null || path.isBlank()) {
            return null;
        }

        JsonNode current = payload;
        for (String segment : path.split("\\.")) {
            if (current == null || current.isMissingNode()) {
                return null;
            }
            current = current.get(segment);
        }

        return current;
    }
}
