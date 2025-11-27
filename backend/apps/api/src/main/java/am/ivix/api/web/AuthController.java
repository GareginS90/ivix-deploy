
package am.ivix.api.web;

import am.ivix.api.security.JwtService;
import am.ivix.users.app.AuthService;
import am.ivix.users.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authManager, AuthService authService, JwtService jwtService) {
        this.authManager = authManager;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        User user = authService.register(email, password);
        return ResponseEntity.ok(Map.of("id", user.getId(), "email", user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = authService.findByEmail(email); // добавь в AuthService вспомогательный метод findByEmail

        var tokens = jwtService.issueTokens(user.getId(), user.getEmail(), List.of("ROLE_USER"));
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        var tokens = jwtService.rotateRefresh(refreshToken);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String accessToken = body.get("accessToken");
        String refreshToken = body.get("refreshToken");
        jwtService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
