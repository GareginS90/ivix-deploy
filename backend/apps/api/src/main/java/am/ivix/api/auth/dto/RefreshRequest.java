package am.ivix.api.auth.dto;

/**
 * Запрос на обновление access-токена по refresh-токену.
 * Production-grade DTO (immutable, minimal, strict).
 */
public record RefreshRequest(
        String refreshToken
) {}

