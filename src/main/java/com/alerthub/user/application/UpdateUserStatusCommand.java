package com.alerthub.user.application;

import com.alerthub.user.domain.UserStatus;

public record UpdateUserStatusCommand(UserStatus status) {
}
