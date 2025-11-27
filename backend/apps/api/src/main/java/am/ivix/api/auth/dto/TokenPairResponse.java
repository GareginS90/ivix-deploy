package am.ivix.api.auth.dto;

import java.time.Instant;
import java.util.Objects;

public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        Instant accessExpiresAt,
        Instant refreshExpiresAt,
        String tokenType
) {
    public TokenPairResponse {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        Objects.requireNonNull(accessExpiresAt, "accessExpiresAt must not be null");
        Objects.requireNonNull(refreshExpiresAt, "refreshExpiresAt must not be null");

        if (tokenType == null || tokenType.isBlank()) {
            tokenType = "Bearer";
        }
    }
}

