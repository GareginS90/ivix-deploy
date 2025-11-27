package am.ivix.api.security.store;

import am.ivix.securitycore.store.TokenJtiStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class RedisTokenJtiStore implements TokenJtiStore {

    private final StringRedisTemplate redis;

    public RedisTokenJtiStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String accessKey(String jti) {
        return "jwt:access:blacklist:" + jti;
    }

    private String refreshKey(String jti) {
        return "jwt:refresh:whitelist:" + jti;
    }

    @Override
    public void blacklistAccess(String jti, Instant expiresAt) {
        long ttlSeconds = Math.max(1, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
        redis.opsForValue().set(accessKey(jti), "1", Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean isAccessBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(accessKey(jti)));
    }

    @Override
    public void whitelistRefresh(String jti, Instant expiresAt) {
        long ttlSeconds = Math.max(1, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
        redis.opsForValue().set(refreshKey(jti), "1", Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean isRefreshWhitelisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(refreshKey(jti)));
    }

    @Override
    public void removeRefresh(String jti) {
        redis.delete(refreshKey(jti));
    }
}
