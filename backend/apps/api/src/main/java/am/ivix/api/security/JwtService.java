package am.ivix.api.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JwtService {

    public enum TokenKind { ACCESS, REFRESH }

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final StringRedisTemplate redis;
    private final String issuer;
    private final String audience;
    private final int accessTtlMinutes;
    private final int refreshTtlDays;

    public JwtService(KeyProvider keys,
                      StringRedisTemplate redis,
                      String issuer,
                      String audience,
                      int accessTtlMinutes,
                      int refreshTtlDays) {

        this.publicKey = (RSAPublicKey) keys.getPublicKey();
        this.privateKey = (RSAPrivateKey) keys.getPrivateKey();
        this.redis = redis;
        this.issuer = issuer;
        this.audience = audience;
        this.accessTtlMinutes = accessTtlMinutes;
        this.refreshTtlDays = refreshTtlDays;
    }

    /** ====================== TOKEN ISSUING ======================== */

    public Map<String, String> issueTokens(UUID userId, String email, List<String> roles) {
        String access = createToken(userId, email, roles, TokenKind.ACCESS, accessTtlMinutes);
        String refresh = createToken(userId, email, roles, TokenKind.REFRESH, refreshTtlDays * 24 * 60);

        String refreshJti = getJtiSafe(refresh);
        whitelistRefresh(refreshJti, refreshTtlDays);

        Map<String, String> out = new HashMap<>();
        out.put("accessToken", access);
        out.put("refreshToken", refresh);
        return out;
    }

    public Map<String, String> rotateRefresh(String refreshToken) {
        JWTClaimsSet claims = validate(refreshToken, TokenKind.REFRESH);
        String oldJti = claims.getJWTID();

        if (!isRefreshWhitelisted(oldJti)) {
            throw new IllegalStateException("Refresh token already used or not whitelisted");
        }

        removeRefreshWhitelist(oldJti);

        UUID userId = UUID.fromString(claims.getSubject());
        String email = safeGetString(claims, "email");
        List<String> roles = safeGetStringList(claims, "roles");

        return issueTokens(userId, email, roles);
    }

    public void logout(String accessToken, String refreshToken) {
        try {
            JWTClaimsSet accessC = validate(accessToken, TokenKind.ACCESS);
            blacklistJti(accessC.getJWTID(), accessC.getExpirationTime().toInstant());
        } catch (Exception ignored) {}

        try {
            JWTClaimsSet refreshC = validate(refreshToken, TokenKind.REFRESH);
            removeRefreshWhitelist(refreshC.getJWTID());
        } catch (Exception ignored) {}
    }

    /** ====================== VALIDATION ======================== */

    public JWTClaimsSet validate(String token, TokenKind expectedKind) {
        try {
            SignedJWT jwt;
            try {
                jwt = SignedJWT.parse(token);
            } catch (java.text.ParseException ex) {
                throw new IllegalStateException("Invalid JWT format", ex);
            }

            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwt.verify(verifier)) {
                throw new IllegalStateException("Signature invalid");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            if (!issuer.equals(claims.getIssuer()))
                throw new IllegalStateException("Invalid iss");

            if (!claims.getAudience().contains(audience))
                throw new IllegalStateException("Invalid aud");

            if (Instant.now().isBefore(claims.getNotBeforeTime().toInstant()))
                throw new IllegalStateException("nbf not reached");

            if (Instant.now().isAfter(claims.getExpirationTime().toInstant()))
                throw new IllegalStateException("Token expired");

            String kind = safeGetString(claims, "typ");

            if (expectedKind == TokenKind.ACCESS && !"access".equals(kind))
                throw new IllegalStateException("Not an access token");

            if (expectedKind == TokenKind.REFRESH && !"refresh".equals(kind))
                throw new IllegalStateException("Not a refresh token");

            if (expectedKind == TokenKind.ACCESS && isBlacklisted(claims.getJWTID()))
                throw new IllegalStateException("Token is revoked");

            return claims;

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token", e);
        }
    }

    /** ====================== TOKEN CREATION ======================== */

    private String createToken(UUID userId, String email, List<String> roles, TokenKind kind, int ttlMinutes) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plus(ttlMinutes, ChronoUnit.MINUTES);

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

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID("ivix-key-1")
                    .build();

            SignedJWT signed = new SignedJWT(header, claims);
            signed.sign(new RSASSASigner(privateKey));

            return signed.serialize();

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create token", ex);
        }
    }

    /** ====================== SAFE CLAIM EXTRACTORS ======================== */

    private String safeGetString(JWTClaimsSet claims, String name) {
        try {
            Object val = claims.getClaim(name);
            return val != null ? val.toString() : null;
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid claim: " + name, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> safeGetStringList(JWTClaimsSet claims, String name) {
        try {
            Object val = claims.getClaim(name);
            if (val == null) return Collections.emptyList();
            return (List<String>) val;
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid list claim: " + name, ex);
        }
    }

    /** ====================== JTI HELPERS ======================== */

    private String getJtiSafe(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getJWTID();
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid token", ex);
        }
    }

    /** ====================== BLACKLIST / WHITELIST ======================== */

    private boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(accessBlacklistKey(jti)));
    }

    private void blacklistJti(String jti, Instant exp) {
        long ttl = Math.max(1, exp.getEpochSecond() - Instant.now().getEpochSecond());
        redis.opsForValue().set(accessBlacklistKey(jti), "1", ttl, TimeUnit.SECONDS);
    }

    private void whitelistRefresh(String jti, int ttlDays) {
        redis.opsForValue().set(refreshWhitelistKey(jti), "1", ttlDays, TimeUnit.DAYS);
    }

    private boolean isRefreshWhitelisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(refreshWhitelistKey(jti)));
    }

    private void removeRefreshWhitelist(String jti) {
        redis.delete(refreshWhitelistKey(jti));
    }

    private String accessBlacklistKey(String jti) {
        return "jwt:blacklist:" + jti;
    }

    private String refreshWhitelistKey(String jti) {
        return "jwt:refresh:whitelist:" + jti;
    }
}

