package am.ivix.securitycore.jwt;

import am.ivix.securitycore.jwt.JwtTokenCreator.IssuedToken;
import am.ivix.securitycore.jwt.JwtTokenValidator.ValidationResult;
import am.ivix.securitycore.store.TokenJtiStore;
import com.nimbusds.jwt.JWTClaimsSet;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class JwtTokenManager {

    public record TokensPair(String accessToken, String refreshToken) {
        public TokensPair {
            Objects.requireNonNull(accessToken, "accessToken must not be null");
            Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        }
    }

    private final JwtTokenCreator creator;
    private final JwtTokenValidator validator;
    private final TokenJtiStore jtiStore;

    public JwtTokenManager(
            JwtTokenCreator creator,
            JwtTokenValidator validator,
            TokenJtiStore jtiStore
    ) {
        this.creator = Objects.requireNonNull(creator);
        this.validator = Objects.requireNonNull(validator);
        this.jtiStore = Objects.requireNonNull(jtiStore);
    }

    public TokensPair issueTokens(UUID userId, String email, List<String> roles) {
        IssuedToken access = creator.createAccessToken(userId, email, roles);
        IssuedToken refresh = creator.createRefreshToken(userId, email, roles);

        jtiStore.whitelistRefresh(refresh.jti(), refresh.expiresAt());

        return new TokensPair(access.token(), refresh.token());
    }

    public TokensPair rotateRefresh(String refreshToken) {

        ValidationResult vr =
                validator.validate(refreshToken, JwtTokenValidator.TokenKind.REFRESH);

        if (!vr.ok) {
            throw new IllegalStateException("Invalid refresh token: " + vr.error);
        }

        JWTClaimsSet claims = vr.claims;
        String jti = claims.getJWTID();

        if (!jtiStore.isRefreshWhitelisted(jti)) {
            throw new IllegalStateException("Refresh token not whitelisted or already used");
        }

        jtiStore.removeRefresh(jti);

        UUID userId = UUID.fromString(claims.getSubject());
        String email;
        List<String> roles;

        try {
            email = claims.getStringClaim("email");

            @SuppressWarnings("unchecked")
            List<String> casted = (List<String>) claims.getClaim("roles");
            roles = casted;

        } catch (Exception e) {
            throw new IllegalStateException("Refresh token missing required claims", e);
        }

        return issueTokens(userId, email, roles);
    }

    public void logout(String accessToken, String refreshToken) {

        if (accessToken != null && !accessToken.isBlank()) {
            ValidationResult accessVr =
                    validator.validate(accessToken, JwtTokenValidator.TokenKind.ACCESS);

            if (accessVr.ok && accessVr.claims != null) {
                JWTClaimsSet c = accessVr.claims;
                String jti = c.getJWTID();

                Instant exp = c.getExpirationTime() != null
                        ? c.getExpirationTime().toInstant()
                        : Instant.now().plusSeconds(60);

                if (jti != null) {
                    jtiStore.blacklistAccess(jti, exp);
                }
            }
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            ValidationResult refreshVr =
                    validator.validate(refreshToken, JwtTokenValidator.TokenKind.REFRESH);

            if (refreshVr.ok && refreshVr.claims != null) {
                String jti = refreshVr.claims.getJWTID();
                if (jti != null) {
                    jtiStore.removeRefresh(jti);
                }
            }
        }
    }

    public boolean isAccessRevoked(String accessToken) {

        ValidationResult vr =
                validator.validate(accessToken, JwtTokenValidator.TokenKind.ACCESS);

        if (!vr.ok || vr.claims == null) {
            return true;
        }

        String jti = vr.claims.getJWTID();
        return jti == null || jtiStore.isAccessBlacklisted(jti);
    }
}

