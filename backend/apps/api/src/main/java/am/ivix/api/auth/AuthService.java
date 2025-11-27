package am.ivix.api.auth;

import am.ivix.api.auth.dto.AuthRequest;
import am.ivix.api.auth.dto.AuthResponse;
import am.ivix.api.auth.dto.RefreshRequest;
import am.ivix.api.auth.dto.LogoutRequest;
import am.ivix.securitycore.jwt.JwtTokenManager;

/**
 * Сервис авторизации.
 *
 * Здесь будет бизнес-логика:
 * - поиск пользователя по email
 * - проверка пароля
 * - вызов JwtTokenManager для выдачи/обновления токенов
 * - logout (blacklist/whitelist через TokenJtiStore внутри JwtTokenManager)
 */
public class AuthService {

    private final JwtTokenManager jwtTokenManager;

    // позже сюда аккуратно добавим UserService / PasswordEncoder и т.д.

    public AuthService(JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }

    /**
     * Логин пользователя по email/password.
     */
    public AuthResponse login(AuthRequest request) {
        // TODO: реализовать после привязки к UserService.
        // Здесь будет:
        // 1) найти пользователя по email
        // 2) проверить пароль
        // 3) получить roles
        // 4) вызвать jwtTokenManager.issueTokens(...)
        //
        // Временно бросаем исключение, чтобы не было "тихой магии".
        throw new UnsupportedOperationException("AuthService.login is not implemented yet");
    }

    /**
     * Обновление токенов по refresh-токену.
     */
    public AuthResponse refresh(RefreshRequest request) {
        // TODO: использовать jwtTokenManager для rotate refresh.
        throw new UnsupportedOperationException("AuthService.refresh is not implemented yet");
    }

    /**
     * Logout: инвалидируем access/refresh.
     */
    public void logout(LogoutRequest request) {
        // TODO: использовать jwtTokenManager.logout(...) (или аналогичный метод).
        throw new UnsupportedOperationException("AuthService.logout is not implemented yet");
    }
}
