package am.ivix.api.web.auth;

import am.ivix.api.app.auth.JwtAuthService;
import am.ivix.api.app.auth.JwtAuthService.TokenPair;
import am.ivix.api.web.auth.dto.LoginRequest;
import am.ivix.api.web.auth.dto.RefreshRequest;
import am.ivix.api.web.auth.dto.RegisterRequest;
import am.ivix.api.web.auth.dto.TokenPairResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtAuthService jwtAuthService;

    public AuthController(JwtAuthService jwtAuthService) {
        this.jwtAuthService = jwtAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenPairResponse> register(@RequestBody RegisterRequest request) {
        TokenPair pair = jwtAuthService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(
                new TokenPairResponse(pair.getAccessToken(), pair.getRefreshToken())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@RequestBody LoginRequest request) {
        TokenPair pair = jwtAuthService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(
                new TokenPairResponse(pair.getAccessToken(), pair.getRefreshToken())
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody RefreshRequest request) {
        TokenPair pair = jwtAuthService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(
                new TokenPairResponse(pair.getAccessToken(), pair.getRefreshToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        jwtAuthService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}

