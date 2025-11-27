package am.ivix.profiles.repo;

import am.ivix.profiles.domain.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
}
