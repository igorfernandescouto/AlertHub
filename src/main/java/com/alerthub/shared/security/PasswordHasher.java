package com.alerthub.shared.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordHasher {

    public String hash(String rawPassword) {
        validate(rawPassword);
        return BcryptUtil.bcryptHash(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        validate(rawPassword);
        return BcryptUtil.matches(rawPassword, passwordHash);
    }

    private void validate(String rawPassword) {
        if (rawPassword != null && rawPassword.length() >= 8) {
            return;
        }
        throw new IllegalArgumentException("password must contain at least 8 characters");
    }
}
