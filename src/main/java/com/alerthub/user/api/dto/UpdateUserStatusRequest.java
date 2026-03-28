package com.alerthub.user.api.dto;

import com.alerthub.user.domain.UserStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {
}
