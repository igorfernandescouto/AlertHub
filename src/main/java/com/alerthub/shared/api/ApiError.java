package com.alerthub.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ApiViolation> violations
) {

    public static ApiError of(int status, String error, String message, String path, List<ApiViolation> violations) {
        return new ApiError(Instant.now(), status, error, message, path, violations);
    }
}
