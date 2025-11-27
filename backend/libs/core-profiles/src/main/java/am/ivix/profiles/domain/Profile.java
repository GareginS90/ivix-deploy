package am.ivix.profiles.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    // Новое поле — имя пользователя для отображения
    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private String type = "PERSON"; // PERSON / COMPANY (по умолчанию человек)

    private String fullName;
    private String companyName;

    private String country;
    private String city;
    private String district;

    private Double lat;
    private Double lon;

    private Double ratingAvg = 0.0;
    private Integer reviewsCount = 0;
    private Integer ordersDoneCount = 0;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // === Constructors ===

    public Profile() {}

    // Создаём профиль сразу при регистрации
    public Profile(UUID userId) {
        this.userId = userId;
        this.type = "PERSON";
        this.displayName = "User"; // можно потом изменить
    }

    // === Getters/Setters ===

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public Double getRatingAvg() { return ratingAvg; }
    public Integer getReviewsCount() { return reviewsCount; }
    public Integer getOrdersDoneCount() { return ordersDoneCount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}

