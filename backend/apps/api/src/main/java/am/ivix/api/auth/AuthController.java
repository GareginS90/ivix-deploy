package am.ivix.api.auth;

import am.ivix.api.auth.dto.LoginRequest;
import am.ivix.api.auth.dto.RefreshRequest;
import am.ivix.api.auth.dto.RegisterRequest;
import am.ivix.api.auth.dto.TokenPairResponse;
import am.ivix.api.auth.dto.UserProfileResponse;
import am.ivix.api.security.JwtAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtAuthService jwtAuthService;

    public AuthController(JwtAuthService jwtAuthService) {
        this.jwtAuthService = jwtAuthService;
    }

    /** REGISTER ---------------------------------------------------------- */
    @PostMapping("/register")
    public ResponseEntity<TokenPairResponse> register(@Valid @RequestBody RegisterRequest req) {
        TokenPairResponse pair = jwtAuthService.register(
                req.email(),
                req.password()
        );
        return ResponseEntity.ok(pair);
    }

    /** LOGIN ------------------------------------------------------------- */
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest req) {
        TokenPairResponse pair = jwtAuthService.login(
                req.email(),
                req.password()
        );
        return ResponseEntity.ok(pair);
    }

    /** REFRESH ----------------------------------------------------------- */
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        TokenPairResponse pair = jwtAuthService.refresh(req.refreshToken());
        return ResponseEntity.ok(pair);
    }

    /** LOGOUT ------------------------------------------------------------ */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        jwtAuthService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    /** ME ---------------------------------------------------------------- */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@RequestAttribute("userId") String userId) {
        UserProfileResponse profile = jwtAuthService.me(userId);
        return ResponseEntity.ok(profile);
    }
}

