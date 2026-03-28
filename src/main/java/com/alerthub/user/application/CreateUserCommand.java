package com.alerthub.user.application;

import com.alerthub.user.domain.UserRole;
import com.alerthub.user.domain.UserStatus;

public record CreateUserCommand(String fullName, String email, String password, UserRole role, UserStatus status) {
}
