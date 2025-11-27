package am.ivix.securitycore.store;

import java.time.Instant;

/**
 * Абстракция над хранилищем JTI (Redis, DB, in-memory и т.д.).
 *
 * Security-core ничего не знает о Spring / Redis —
 * только об этой интерфейсной "прослойке".
 */
public interface TokenJtiStore {

    /**
     * Поместить access-токен в blacklist до момента expiresAt.
     */
    void blacklistAccess(String jti, Instant expiresAt);

    /**
     * Проверить, что access-токен с таким jti уже отозван.
     */
    boolean isAccessBlacklisted(String jti);

    /**
     * Добавить refresh-токен в whitelist до момента expiresAt.
     */
    void whitelistRefresh(String jti, Instant expiresAt);

    /**
     * Проверить, что refresh-токен с таким jti всё ещё разрешён (не использован).
     */
    boolean isRefreshWhitelisted(String jti);

    /**
     * Удалить refresh-токен из whitelist (после rotate или logout).
     */
    void removeRefresh(String jti);
}

