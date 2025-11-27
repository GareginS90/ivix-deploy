package am.ivix.profiles.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProfileSpecializationId implements Serializable {
    private UUID profileId;
    private UUID specializationId;
}

