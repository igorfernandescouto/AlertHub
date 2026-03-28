package com.alerthub.auth.api.dto;

public record AuthTokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
