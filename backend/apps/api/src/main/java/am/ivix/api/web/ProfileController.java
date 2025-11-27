package am.ivix.api.web;

import am.ivix.profiles.app.ProfileService;
import am.ivix.profiles.domain.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public Profile me(Authentication auth) {
        // auth.getName() содержит userId (UUID), передаваемый из AuthService при login
        UUID userId = UUID.fromString(auth.getName());
        return profileService.getProfile(userId);
    }
}

