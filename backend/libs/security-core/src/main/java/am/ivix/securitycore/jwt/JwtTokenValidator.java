package am.ivix.securitycore.jwt;

import am.ivix.securitycore.keys.KeyProvider;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Production-grade JWT validator.
 *
 * Отвечает ТОЛЬКО за валидацию токена, без Redis и без Spring.
 * Используется на верхнем уровне (api) для проверки ключевых полей.
 */
public final class JwtTokenValidator {

    public enum TokenKind { ACCESS, REFRESH }

    public static final class ValidationResult {
        public final boolean ok;
        public final String error;
        public final JWTClaimsSet claims;

        private ValidationResult(boolean ok, String error, JWTClaimsSet claims) {
            this.ok = ok;
            this.error = error;
            this.claims = claims;
        }

        public static ValidationResult success(JWTClaimsSet claims) {
            return new ValidationResult(true, null, claims);
        }

        public static ValidationResult fail(String error) {
            return new ValidationResult(false, error, null);
        }
    }

    private final RSAPublicKey publicKey;
    private final String issuer;
    private final String audience;

    public JwtTokenValidator(KeyProvider keyProvider, String issuer, String audience) {
        Objects.requireNonNull(keyProvider, "keyProvider must not be null");
        Objects.requireNonNull(issuer, "issuer must not be null");
        Objects.requireNonNull(audience, "audience must not be null");

        PublicKey pk = keyProvider.getPublicKey();
        if (!(pk instanceof RSAPublicKey rsaPublicKey)) {
            throw new IllegalArgumentException("KeyProvider must return RSAPublicKey for JWT validation");
        }

        this.publicKey = rsaPublicKey;
        this.issuer = issuer;
        this.audience = audience;
    }

    /**
     * Основной метод проверки токена.
     *
     * НЕ выбрасывает исключений — возвращает ValidationResult.
     * Это production-best-practice для security слоя.
     */
    public ValidationResult validate(String token, TokenKind expectedKind) {
        if (token == null || token.isBlank()) {
            return ValidationResult.fail("Token is null or empty");
        }

        SignedJWT jwt;
        try {
            jwt = SignedJWT.parse(token);
        } catch (ParseException e) {
            return ValidationResult.fail("Invalid JWT format: " + e.getMessage());
        }

        // 1. Проверка подписи
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        try {
            if (!jwt.verify(verifier)) {
                return ValidationResult.fail("Signature invalid");
            }
        } catch (JOSEException e) {
            return ValidationResult.fail("Signature verification error: " + e.getMessage());
        }

        // 2. Парсим claims
        JWTClaimsSet claims;
        try {
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            return ValidationResult.fail("Claims parsing failed: " + e.getMessage());
        }

        // 3. Проверка issuer
        if (!issuer.equals(claims.getIssuer())) {
            return ValidationResult.fail("Invalid issuer");
        }

        // 4. Проверка audience
        List<String> aud = claims.getAudience();
        if (aud == null || !aud.contains(audience)) {
            return ValidationResult.fail("Invalid audience");
        }

        Instant now = Instant.now();

        // 5. nbf (Not Before)
        if (claims.getNotBeforeTime() != null) {
            Instant nbf = claims.getNotBeforeTime().toInstant();
            if (now.isBefore(nbf)) {
                return ValidationResult.fail("Token not active yet (nbf)");
            }
        }

        // 6. exp (Expiration)
        if (claims.getExpirationTime() != null) {
            Instant exp = claims.getExpirationTime().toInstant();
            if (now.isAfter(exp)) {
                return ValidationResult.fail("Token expired");
            }
        }

        // 7. Проверка тип токена (access / refresh)
        Object typObj = claims.getClaim("typ");
        if (!(typObj instanceof String typ)) {
            return ValidationResult.fail("Invalid typ claim");
        }

        switch (expectedKind) {
            case ACCESS -> {
                if (!"access".equals(typ)) {
                    return ValidationResult.fail("Expected access token, but got: " + typ);
                }
            }
            case REFRESH -> {
                if (!"refresh".equals(typ)) {
                    return ValidationResult.fail("Expected refresh token, but got: " + typ);
                }
            }
        }

        // Всё хорошо
        return ValidationResult.success(claims);
    }
}
