package am.ivix.api.security;

import am.ivix.api.auth.dto.TokenPairResponse;
import am.ivix.api.auth.dto.UserProfileResponse;
import am.ivix.securitycore.jwt.JwtTokenCreator;
import am.ivix.securitycore.jwt.JwtTokenValidator;
import am.ivix.securitycore.store.TokenJtiStore;
import am.ivix.users.app.AuthService;
import am.ivix.users.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JwtAuthService {

    private final AuthService authService;
    private final JwtTokenCreator tokenCreator;
    private final JwtTokenValidator tokenValidator;
    private final TokenJtiStore tokenJtiStore;

    public JwtAuthService(AuthService authService,
                          JwtTokenCreator tokenCreator,
                          JwtTokenValidator tokenValidator,
                          TokenJtiStore tokenJtiStore) {
        this.authService = authService;
        this.tokenCreator = tokenCreator;
        this.tokenValidator = tokenValidator;
        this.tokenJtiStore = tokenJtiStore;
    }

    /**
     * Вспомогательный метод: выпускает пару токенов и записывает refresh в whitelist.
     */
    private TokenPairResponse issueTokenPair(User user) {
        var access = tokenCreator.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRoles()
        );

        var refresh = tokenCreator.createRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getRoles()
        );

        // refresh — stateful, работаем через whitelist
        tokenJtiStore.whitelistRefresh(refresh.jti(), refresh.expiresAt());

        return new TokenPairResponse(
                access.token(),
                refresh.token(),
                access.expiresAt(),
                refresh.expiresAt(),
                "Bearer"
        );
    }

    /** REGISTER --------------------------------------------------------------- */
    public TokenPairResponse register(String email, String password) {
        User user = authService.register(email, password);
        return issueTokenPair(user);
    }

    /** LOGIN ------------------------------------------------------------------ */
    public TokenPairResponse login(String email, String password) {
        User user = authService.login(email, password);
        return issueTokenPair(user);
    }

    /** REFRESH ---------------------------------------------------------------- */
    public TokenPairResponse refresh(String refreshToken) {
        var result = tokenValidator.validate(refreshToken, JwtTokenValidator.TokenKind.REFRESH);

        if (!result.ok) {
            throw new RuntimeException("Invalid refresh token: " + result.error);
        }

        var claims = result.claims;

        String oldJti = claims.getJWTID();
        if (!tokenJtiStore.isRefreshWhitelisted(oldJti)) {
            throw new RuntimeException("Refresh token is not whitelisted or already used");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        String email = (String) claims.getClaim("email");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.getClaim("roles");

        // rotate: старый refresh больше не валиден
        tokenJtiStore.removeRefresh(oldJti);

        var access = tokenCreator.createAccessToken(userId, email, roles);
        var refresh = tokenCreator.createRefreshToken(userId, email, roles);

        tokenJtiStore.whitelistRefresh(refresh.jti(), refresh.expiresAt());

        return new TokenPairResponse(
                access.token(),
                refresh.token(),
                access.expiresAt(),
                refresh.expiresAt(),
                "Bearer"
        );
    }

    /** LOGOUT ----------------------------------------------------------------- */
    public void logout(String refreshToken) {
        var result = tokenValidator.validate(refreshToken, JwtTokenValidator.TokenKind.REFRESH);

        if (!result.ok) {
            // idempotent logout — молча выходим
            return;
        }

        String jti = result.claims.getJWTID();
        tokenJtiStore.removeRefresh(jti);
    }

    /** ME --------------------------------------------------------------------- */
    public UserProfileResponse me(String userIdOrEmail) {
        // Сейчас используем email как ключ (как и в твоём AuthService).
        // Если решим перейти на UUID — добавим findById в AuthService.
        User user = authService.findByEmail(userIdOrEmail);
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getRoles());
    }
}

