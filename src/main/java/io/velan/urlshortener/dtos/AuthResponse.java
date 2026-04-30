package io.velan.urlshortener.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private final String token;
    private final String username;
    private final String role;
}
