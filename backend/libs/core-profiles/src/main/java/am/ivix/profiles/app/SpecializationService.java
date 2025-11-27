package am.ivix.profiles.app;

import am.ivix.profiles.domain.*;
import am.ivix.profiles.repo.ProfileRepository;
import am.ivix.profiles.repo.ProfileSpecializationRepository;
import am.ivix.profiles.repo.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpecializationService {

    private final ProfileRepository profileRepository;
    private final SpecializationRepository specializationRepository;
    private final ProfileSpecializationRepository profileSpecializationRepository;

    @Transactional
    public ProfileSpecialization addSpecializationToProfile(
            UUID profileId,
            UUID specializationId,
            Integer level,
            VerificationStatus verificationStatus
    ) {

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new RuntimeException("Specialization not found"));

        ProfileSpecializationId id = new ProfileSpecializationId(profileId, specializationId);

        ProfileSpecialization ps = ProfileSpecialization.builder()
                .id(id)
                .profile(profile)
                .specialization(specialization)
                .level(level)
                .verificationStatus(verificationStatus)
                .ratingAvg(0.0)
                .reviewsCount(0)
                .build();

        return profileSpecializationRepository.save(ps);
    }
}

