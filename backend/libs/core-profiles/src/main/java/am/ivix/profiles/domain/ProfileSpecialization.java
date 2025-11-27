package am.ivix.profiles.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile_specializations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileSpecialization {

    @EmbeddedId
    private ProfileSpecializationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("profileId")
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("specializationId")
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "rating_avg", columnDefinition = "DOUBLE PRECISION")
    private Double ratingAvg = 0.0;

    @Column(name = "reviews_count")
    private Integer reviewsCount = 0;
}

