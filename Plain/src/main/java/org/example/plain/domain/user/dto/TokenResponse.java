package org.example.plain.domain.user.dto;

public record TokenResponse(
        String userId,
        String accessToken
) {
}
