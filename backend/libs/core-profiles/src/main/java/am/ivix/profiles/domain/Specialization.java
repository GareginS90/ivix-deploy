package am.ivix.profiles.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "specializations")
public class Specialization {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String category;

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
}
