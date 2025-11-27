package am.ivix.securitycore.store;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Реализация TokenJtiStore на Redis.
 *
 * Хранит:
 * - blacklist для access-токенов
 * - whitelist для refresh-токенов
 *
 * Ключи в Redis:
 * - jwt:access:blacklist:{jti}
 * - jwt:refresh:whitelist:{jti}
 */
@Component
public final class RedisTokenJtiStore implements TokenJtiStore {

    private static final String ACCESS_BLACKLIST_PREFIX = "jwt:access:blacklist:";
    private static final String REFRESH_WHITELIST_PREFIX = "jwt:refresh:whitelist:";

    private final StringRedisTemplate redis;

    public RedisTokenJtiStore(StringRedisTemplate redis) {
        this.redis = Objects.requireNonNull(redis, "redis must not be null");
    }

    @Override
    public void blacklistAccess(String jti, Instant expiresAt) {
        if (jti == null || expiresAt == null) {
            return;
        }
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            // Уже истекло — нет смысла писать в Redis
            return;
        }
        String key = ACCESS_BLACKLIST_PREFIX + jti;
        redis.opsForValue().set(key, "1", ttl);
    }

    @Override
    public boolean isAccessBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        String key = ACCESS_BLACKLIST_PREFIX + jti;
        Boolean hasKey = redis.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }

    @Override
    public void whitelistRefresh(String jti, Instant expiresAt) {
        if (jti == null || expiresAt == null) {
            return;
        }
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            // Уже истекло — нет смысла писать в Redis
            return;
        }
        String key = REFRESH_WHITELIST_PREFIX + jti;
        redis.opsForValue().set(key, "1", ttl);
    }

    @Override
    public boolean isRefreshWhitelisted(String jti) {
        if (jti == null) {
            return false;
        }
        String key = REFRESH_WHITELIST_PREFIX + jti;
        Boolean hasKey = redis.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }

    @Override
    public void removeRefresh(String jti) {
        if (jti == null) {
            return;
        }
        String key = REFRESH_WHITELIST_PREFIX + jti;
        redis.delete(key);
    }
}

