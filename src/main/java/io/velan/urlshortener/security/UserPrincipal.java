package io.velan.urlshortener.security;

import io.velan.urlshortener.enums.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserPrincipal {

    private final Long userId;
    private final String role;

    public boolean isAdmin() {
        return Role.ADMIN.name().equals(role);
    }
}
