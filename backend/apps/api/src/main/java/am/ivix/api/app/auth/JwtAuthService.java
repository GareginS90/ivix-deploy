package am.ivix.api.app.auth;

import am.ivix.securitycore.jwt.JwtTokenCreator;
import am.ivix.securitycore.jwt.JwtTokenCreator.IssuedToken;
import am.ivix.securitycore.jwt.JwtTokenValidator;
import am.ivix.securitycore.jwt.JwtTokenValidator.TokenKind;
import am.ivix.securitycore.jwt.JwtTokenValidator.ValidationResult;
import am.ivix.securitycore.store.TokenJtiStore;
import am.ivix.users.app.AuthService;
import am.ivix.users.domain.User;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JwtAuthService {

    private final AuthService authService;
    private final JwtTokenCreator tokenCreator;
    private final JwtTokenValidator tokenValidator;
    private final TokenJtiStore tokenJtiStore;

    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = Objects.requireNonNull(accessToken);
            this.refreshToken = Objects.requireNonNull(refreshToken);
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    public JwtAuthService(
            AuthService authService,
            JwtTokenCreator tokenCreator,
            JwtTokenValidator tokenValidator,
            TokenJtiStore tokenJtiStore
    ) {
        this.authService = authService;
        this.tokenCreator = tokenCreator;
        this.tokenValidator = tokenValidator;
        this.tokenJtiStore = tokenJtiStore;
    }

    /** ---------------------------
     *  REGISTER
     * --------------------------- */
    public TokenPair register(String email, String rawPassword) {
        User user = authService.register(email, rawPassword);
        return issueTokensForUser(user);
    }

    /** ---------------------------
     *  LOGIN
     * --------------------------- */
    public TokenPair login(String email, String rawPassword) {
        User user = authService.login(email, rawPassword);
        return issueTokensForUser(user);
    }

    /** ---------------------------
     *  REFRESH
     *  - validate refresh JWT
     *  - check jti in whitelist
     *  - remove old jti (one-time)
     *  - issue new pair, whitelist new refresh
     * --------------------------- */
    public TokenPair refresh(String refreshToken) {
        ValidationResult result = tokenValidator.validate(refreshToken, TokenKind.REFRESH);
        if (!result.ok) {
            throw new RuntimeException("Refresh token invalid: " + result.error);
        }

        JWTClaimsSet claims = result.claims;

        String jti = claims.getJWTID();
        if (!tokenJtiStore.isRefreshWhitelisted(jti)) {
            throw new RuntimeException("Refresh token is not whitelisted (maybe reused or revoked)");
        }

        // refresh-токен одноразовый — после использования убираем
        tokenJtiStore.removeRefresh(jti);

        String email;
        try {
            email = claims.getStringClaim("email");
        } catch (Exception e) {
            throw new RuntimeException("Refresh token missing 'email' claim", e);
        }

        // контроль, что пользователь ещё существует / активен
        User user = authService.findByEmail(email);

        return issueTokensForUser(user);
    }

    /** ---------------------------
     *  LOGOUT
     *  - просто выкидываем refresh из whitelist
     *  - access сам умрёт по exp
     * --------------------------- */
    public void logout(String refreshToken) {
        ValidationResult result = tokenValidator.validate(refreshToken, TokenKind.REFRESH);
        if (!result.ok) {
            // токен уже протух / битый — делать нечего
            return;
        }

        String jti = result.claims.getJWTID();
        tokenJtiStore.removeRefresh(jti);
    }

    /** ---------------------------
     *  Вспомогательное: выпускаем пару токенов
     * --------------------------- */
    private TokenPair issueTokensForUser(User user) {
        // Пока все обычные пользователи — USER.
        // Позже заменим на реальные роли из user.getRoles().
        List<String> roles = List.of("USER");

        IssuedToken access = tokenCreator.createAccessToken(
                user.getId(),
                user.getEmail(),
                roles
        );

        IssuedToken refresh = tokenCreator.createRefreshToken(
                user.getId(),
                user.getEmail(),
                roles
        );

        // whitelist для refresh (одноразовый токен, контролируем reuse)
        tokenJtiStore.whitelistRefresh(refresh.jti(), refresh.expiresAt());

        return new TokenPair(access.token(), refresh.token());
    }
}

