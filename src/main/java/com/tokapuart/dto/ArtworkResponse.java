package com.tokapuart.dto;

import com.tokapuart.model.Artwork;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtworkResponse {

    private Long id;
    private Long userId;
    private String authorUsername;
    private String authorName;
    private String title;
    private String description;
    private String artistName;
    private Artwork.ArtworkType artworkType;
    private String technique;
    private Integer yearCreated;

    // Ubicación
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private String city;

    // Estadísticas
    private Integer viewsCount;
    private Integer favoritesCount;
    private Integer commentsCount;
    private Integer validationScore;

    // Reacciones
    private Integer likesCount;
    private Integer lovesCount;
    private Integer wowsCount;
    private Integer clapsCount;
    private String userReaction; // La reacción del usuario actual (LIKE, LOVE, WOW, CLAP, o null)

    // Estado
    private Artwork.ArtworkStatus status;
    private Boolean isActive;
    private Boolean isFavorited; // Para el usuario actual

    // Fechas
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Fotos
    private String primaryPhotoUrl;
    private List<PhotoResponse> photos = new ArrayList<>();

    // Comentarios recientes
    private List<CommentResponse> recentComments = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoResponse {
        private Long id;
        private String photoUrl;
        private Boolean isPrimary;
        private Integer orderIndex;
    }
}
