package am.ivix.profiles.repo;

import am.ivix.profiles.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    // Найти профиль по userId (он же PK)
    Optional<Profile> findByUserId(UUID userId);
}

