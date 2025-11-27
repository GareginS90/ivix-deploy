package am.ivix.securitycore.jwt;

import am.ivix.securitycore.keys.KeyProvider;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Отвечает ТОЛЬКО за создание JWT.
 *
 * - Подписывает RS256 с приватным RSA-ключом
 * - Генерирует jti, exp, nbf, iat
 * - Не знает ни про Redis, ни про Spring
 * - Возвращает IssuedToken (token + jti + expiresAt)
 */
public final class JwtTokenCreator {

    /**
     * Тип токена в наших клеймах.
     * Значение идёт в claim "typ": access | refresh
     */
    public enum TokenKind {
        ACCESS,
        REFRESH
    }

    /**
     * Результат выпуска токена:
     * - сам компактный JWT (string)
     * - jti, который можно использовать для blacklist/whitelist
     * - время истечения (exp)
     */
    public record IssuedToken(
            String token,
            String jti,
            Instant expiresAt,
            TokenKind kind
    ) {
        public IssuedToken {
            Objects.requireNonNull(token, "token must not be null");
            Objects.requireNonNull(jti, "jti must not be null");
            Objects.requireNonNull(expiresAt, "expiresAt must not be null");
            Objects.requireNonNull(kind, "kind must not be null");
        }
    }

    private final RSAPrivateKey privateKey;
    private final String issuer;
    private final String audience;
    private final int accessTtlMinutes;
    private final int refreshTtlDays;
    private final JWSHeader header;
    private final JWSSigner signer;

    /**
     * @param keyProvider      поставщик ключей (RSA)
     * @param issuer           iss
     * @param audience         aud
     * @param accessTtlMinutes TTL access-токена в минутах
     * @param refreshTtlDays   TTL refresh-токена в днях
     */
    public JwtTokenCreator(
            KeyProvider keyProvider,
            String issuer,
            String audience,
            int accessTtlMinutes,
            int refreshTtlDays
    ) {
        Objects.requireNonNull(keyProvider, "keyProvider must not be null");
        Objects.requireNonNull(issuer, "issuer must not be null");
        Objects.requireNonNull(audience, "audience must not be null");

        PrivateKey pk = keyProvider.getPrivateKey();
        if (!(pk instanceof RSAPrivateKey rsaPrivateKey)) {
            throw new IllegalArgumentException("KeyProvider must return RSAPrivateKey for JWT signing");
        }

        this.privateKey = rsaPrivateKey;
        this.issuer = issuer;
        this.audience = audience;
        this.accessTtlMinutes = accessTtlMinutes;
        this.refreshTtlDays = refreshTtlDays;

        this.header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                // На проде здесь можно ставить kid, если ключи ротуются
                .keyID("ivix-key-1")
                .build();

        this.signer = new RSASSASigner(this.privateKey);
    }

    /**
     * Создаёт access-токен для пользователя.
     */
    public IssuedToken createAccessToken(
            UUID userId,
            String email,
            List<String> roles
    ) {
        return createToken(userId, email, roles, TokenKind.ACCESS, accessTtlMinutes, ChronoUnit.MINUTES);
    }

    /**
     * Создаёт refresh-токен для пользователя.
     */
    public IssuedToken createRefreshToken(
            UUID userId,
            String email,
            List<String> roles
    ) {
        int ttlMinutes = Math.toIntExact((long) refreshTtlDays * 24L * 60L);
        return createToken(userId, email, roles, TokenKind.REFRESH, ttlMinutes, ChronoUnit.MINUTES);
    }

    /**
     * Базовый метод создания токена.
     */
    private IssuedToken createToken(
            UUID userId,
            String email,
            List<String> roles,
            TokenKind kind,
            int ttlAmount,
            ChronoUnit ttlUnit
    ) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(roles, "roles must not be null");
        Objects.requireNonNull(kind, "kind must not be null");

        try {
            Instant now = Instant.now();
            Instant exp = now.plus(ttlAmount, ttlUnit);
            String jti = UUID.randomUUID().toString();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .issuer(issuer)
                    .audience(audience)
                    .issueTime(Date.from(now))
                    .notBeforeTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .jwtID(jti)
                    .claim("typ", kind == TokenKind.ACCESS ? "access" : "refresh")
                    .claim("email", email)
                    .claim("roles", roles)
                    .build();

            SignedJWT signed = new SignedJWT(header, claims);
            signed.sign(signer);

            String token = signed.serialize();
            return new IssuedToken(token, jti, exp, kind);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create " + kind + " JWT", e);
        }
    }
}
