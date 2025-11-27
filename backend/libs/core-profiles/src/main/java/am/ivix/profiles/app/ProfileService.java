package am.ivix.profiles.app;

import am.ivix.profiles.domain.Profile;
import am.ivix.profiles.repo.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profiles;

    public ProfileService(ProfileRepository profiles) {
        this.profiles = profiles;
    }

    public Profile getProfile(UUID userId) {
        return profiles.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    // ✅ создаёт базовый профиль если отсутствует
    @Transactional
    public Profile createBasicProfile(UUID userId, String type, String displayName) {
        if (profiles.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Profile already exists");
        }

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setType(type);
        profile.setFullName("PERSON".equals(type) ? displayName : null);
        profile.setCompanyName("COMPANY".equals(type) ? displayName : null);
        profile.setDisplayName(displayName);

        return profiles.save(profile);
    }
}

