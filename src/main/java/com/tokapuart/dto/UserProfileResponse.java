package com.tokapuart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String city;
    private String profilePhotoUrl;
    private Boolean isArtist;
    private Boolean isPublic;

    // Estadísticas
    private Long artworksCount;
    private Long favoritesCount;
    private Long commentsCount;
    private Long followersCount;
    private Long followingCount;

    private LocalDateTime createdAt;
    private Boolean isFollowing; // Solo si se consulta desde otro usuario
}
