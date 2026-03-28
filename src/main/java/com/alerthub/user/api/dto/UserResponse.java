package com.alerthub.user.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.alerthub.user.domain.AppUser;
import com.alerthub.user.domain.UserRole;
import com.alerthub.user.domain.UserStatus;

public record UserResponse(
        UUID id,
        UUID tenantId,
        String fullName,
        String email,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
