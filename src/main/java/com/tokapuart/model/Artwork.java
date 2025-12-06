package com.tokapuart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "artworks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "artist_name", length = 100)
    private String artistName;

    @Enumerated(EnumType.STRING)
    @Column(name = "artwork_type", nullable = false)
    private ArtworkType artworkType;

    @Column(length = 100)
    private String technique;

    @Column(name = "year_created")
    private Integer yearCreated;

    // Geolocalización
    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    // Estadísticas
    @Column(name = "views_count")
    private Integer viewsCount = 0;

    @Column(name = "favorites_count")
    private Integer favoritesCount = 0;

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Column(name = "validation_score")
    private Integer validationScore = 0;

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Column(name = "loves_count")
    private Integer lovesCount = 0;

    @Column(name = "wows_count")
    private Integer wowsCount = 0;

    @Column(name = "claps_count")
    private Integer clapsCount = 0;

    // Estado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArtworkStatus status = ArtworkStatus.PENDING;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones
    @OneToMany(mappedBy = "artwork", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArtworkPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "artwork", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Favorite> favorites = new HashSet<>();

    @OneToMany(mappedBy = "artwork", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "artwork", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Validation> validations = new ArrayList<>();

    @OneToMany(mappedBy = "artwork", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ArtworkReaction> reactions = new HashSet<>();

    public enum ArtworkType {
        MURAL, GRAFFITI, ESCULTURA, INSTALACION, OTRO
    }

    public enum ArtworkStatus {
        PENDING, APPROVED, REJECTED, REMOVED
    }
}
