package am.ivix.api.web.profiles;

import am.ivix.profiles.app.SpecializationService;
import am.ivix.profiles.domain.VerificationStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class SpecializationController {

    private final SpecializationService service;

    @PostMapping("/{profileId}/specializations/{specializationId}")
    public void addSpecialization(
            @PathVariable UUID profileId,
            @PathVariable UUID specializationId,
            @RequestBody AddSpecializationRequest request
    ) {
        service.addSpecializationToProfile(
                profileId,
                specializationId,
                request.level(),
                request.verificationStatus()
        );
    }

    public record AddSpecializationRequest(
            Integer level,
            VerificationStatus verificationStatus
    ) {}
}

