package com.alerthub.user.api.dto;

import com.alerthub.user.domain.UserRole;
import com.alerthub.user.domain.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 8, max = 120) String password,
        @NotNull UserRole role,
        @NotNull UserStatus status
) {
}
