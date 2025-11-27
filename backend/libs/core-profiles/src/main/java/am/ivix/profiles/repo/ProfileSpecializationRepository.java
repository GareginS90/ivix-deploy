package am.ivix.profiles.repo;

import am.ivix.profiles.domain.ProfileSpecialization;
import am.ivix.profiles.domain.ProfileSpecializationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileSpecializationRepository extends JpaRepository<ProfileSpecialization, ProfileSpecializationId> {
}
