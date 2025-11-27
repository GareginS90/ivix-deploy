package am.ivix.api.web.profiles;

import am.ivix.profiles.app.ProfileService;
import am.ivix.users.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileSetupController {

    private static final Logger log = LoggerFactory.getLogger(ProfileSetupController.class);

    private final ProfileService profiles;
    private final UserRepository users;

    public ProfileSetupController(ProfileService profiles, UserRepository users) {
        this.profiles = profiles;
        this.users = users;
    }

    @PostMapping("/setup")
    public Object setup(Principal principal, @RequestBody Map<String, String> body) {
        log.info("📩 POST /api/profile/setup called by {}", principal.getName());

        String email = principal.getName(); // email пользователя
        UUID userId = users.findByEmailIgnoreCase(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String type = body.getOrDefault("type", "PERSON");
        String displayName = body.getOrDefault("displayName", "Unknown User");

        log.info("🧩 Creating profile for userId={}, type={}, displayName={}", userId, type, displayName);

        var profile = profiles.createBasicProfile(userId, type, displayName);

        log.info("✅ Profile created: {}", profile);

        return profile;
    }
}

