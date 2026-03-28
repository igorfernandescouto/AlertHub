package com.alerthub.system.api;

import java.time.Instant;

public record SystemStatusResponse(String name, String version, Instant timestamp) {
}
