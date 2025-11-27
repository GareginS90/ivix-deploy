package am.ivix.securitycore.keys;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Абстракция над источником ключей.
 *
 * Это может быть:
 *  - локальные файлы (PEM)
 *  - Hashicorp Vault
 *  - AWS KMS
 *  - Yandex Lockbox
 *  - HSM модуль
 *  - PostgreSQL encrypted store
 *
 * В продакшене мы сможем переключать реализацию без изменения JwtService.
 */
public interface KeyProvider {

    /**
     * Возвращает RSA публичный ключ, используемый для валидации JWT.
     */
    PublicKey getPublicKey();

    /**
     * Возвращает RSA приватный ключ, используемый для подписи JWT.
     */
    PrivateKey getPrivateKey();
}
