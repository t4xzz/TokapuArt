package com.tokapuart.service;

import com.tokapuart.dto.ArtworkRequest;
import com.tokapuart.dto.ArtworkResponse;
import com.tokapuart.dto.CommentResponse;
import com.tokapuart.model.*;
import com.tokapuart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkPhotoRepository artworkPhotoRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final ArtworkReactionRepository artworkReactionRepository;

    @Value("${server.base-url:http://10.0.2.2:8080}")
    private String serverBaseUrl;

    private String buildFullImageUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            return null;
        }
        // Si ya es una URL completa, devolverla tal cual
        if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
            return photoUrl;
        }
        // Construir URL completa
        return serverBaseUrl + photoUrl;
    }

    @Transactional
    public ArtworkResponse createArtwork(ArtworkRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Artwork artwork = Artwork.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .artistName(request.getArtistName())
                .artworkType(request.getArtworkType())
                .technique(request.getTechnique())
                .yearCreated(request.getYearCreated())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .city(request.getCity())
                .status(Artwork.ArtworkStatus.APPROVED) // Auto-aprobar por ahora
                .isActive(true)
                .viewsCount(0)
                .favoritesCount(0)
                .commentsCount(0)
                .validationScore(0)
                .likesCount(0)
                .lovesCount(0)
                .wowsCount(0)
                .clapsCount(0)
                .build();

        artwork = artworkRepository.save(artwork);

        return mapToResponse(artwork, userId);
    }

    @Transactional(readOnly = true)
    public List<ArtworkResponse> getAllArtworks(Long currentUserId) {
        return artworkRepository.findAllApproved().stream()
                .map(artwork -> mapToResponse(artwork, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArtworkResponse getArtworkById(Long id, Long currentUserId) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada"));

        // Incrementar contador de vistas (manejar null)
        Integer currentViews = artwork.getViewsCount() != null ? artwork.getViewsCount() : 0;
        artwork.setViewsCount(currentViews + 1);
        artworkRepository.save(artwork);

        return mapToResponse(artwork, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<ArtworkResponse> getArtworksByUserId(Long userId, Long currentUserId) {
        return artworkRepository.findByUserId(userId).stream()
                .map(artwork -> mapToResponse(artwork, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArtworkResponse> searchArtworks(String query, Long currentUserId) {
        return artworkRepository.searchArtworks(query).stream()
                .map(artwork -> mapToResponse(artwork, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArtworkResponse> filterByType(Artwork.ArtworkType type, Long currentUserId) {
        return artworkRepository.findByArtworkTypeAndApproved(type).stream()
                .map(artwork -> mapToResponse(artwork, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArtworkResponse> getNearbyArtworks(BigDecimal lat, BigDecimal lng, Double radiusKm, Long currentUserId) {
        // Calcular aproximación simple de radio en grados
        double latDelta = radiusKm / 111.0; // 1 grado lat ≈ 111 km
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat.doubleValue())));

        BigDecimal minLat = lat.subtract(BigDecimal.valueOf(latDelta));
        BigDecimal maxLat = lat.add(BigDecimal.valueOf(latDelta));
        BigDecimal minLng = lng.subtract(BigDecimal.valueOf(lngDelta));
        BigDecimal maxLng = lng.add(BigDecimal.valueOf(lngDelta));

        return artworkRepository.findNearby(minLat, maxLat, minLng, maxLng).stream()
                .map(artwork -> mapToResponse(artwork, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public ArtworkResponse updateArtwork(Long id, ArtworkRequest request, Long userId) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada"));

        if (!artwork.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para editar esta obra");
        }

        artwork.setTitle(request.getTitle());
        artwork.setDescription(request.getDescription());
        artwork.setArtistName(request.getArtistName());
        artwork.setArtworkType(request.getArtworkType());
        artwork.setTechnique(request.getTechnique());
        artwork.setYearCreated(request.getYearCreated());

        artwork = artworkRepository.save(artwork);

        return mapToResponse(artwork, userId);
    }

    @Transactional
    public void deleteArtwork(Long id, Long userId) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada"));

        if (!artwork.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta obra");
        }

        artworkRepository.delete(artwork);
    }

    private ArtworkResponse mapToResponse(Artwork artwork, Long currentUserId) {
        // Obtener fotos
        List<ArtworkPhoto> photos = artworkPhotoRepository.findByArtworkIdOrderByOrderIndexAsc(artwork.getId());

        // Obtener foto principal y construir URL completa
        String primaryPhotoUrl = photos.stream()
                .filter(ArtworkPhoto::getIsPrimary)
                .findFirst()
                .map(ArtworkPhoto::getPhotoUrl)
                .or(() -> photos.stream().findFirst().map(ArtworkPhoto::getPhotoUrl))
                .map(this::buildFullImageUrl)
                .orElse(null);

        // Obtener comentarios recientes (últimos 3)
        List<Comment> comments = commentRepository.findTop3ByArtworkIdOrderByCreatedAtDesc(artwork.getId());
        List<CommentResponse> commentResponses = comments.stream()
                .limit(3)
                .map(this::mapCommentToResponse)
                .collect(Collectors.toList());

        // Verificar si el usuario actual tiene esta obra como favorita
        Boolean isFavorited = currentUserId != null &&
                favoriteRepository.existsByUserIdAndArtworkId(currentUserId, artwork.getId());

        // Obtener reacción del usuario actual
        String userReaction = null;
        if (currentUserId != null) {
            userReaction = artworkReactionRepository.findByUserIdAndArtworkId(currentUserId, artwork.getId())
                    .map(reaction -> reaction.getReactionType().name())
                    .orElse(null);
        }

        return ArtworkResponse.builder()
                .id(artwork.getId())
                .userId(artwork.getUser().getId())
                .authorUsername(artwork.getUser().getUsername())
                .authorName(artwork.getUser().getFullName())
                .title(artwork.getTitle())
                .description(artwork.getDescription())
                .artistName(artwork.getArtistName())
                .artworkType(artwork.getArtworkType())
                .technique(artwork.getTechnique())
                .yearCreated(artwork.getYearCreated())
                .latitude(artwork.getLatitude())
                .longitude(artwork.getLongitude())
                .address(artwork.getAddress())
                .city(artwork.getCity())
                .viewsCount(artwork.getViewsCount())
                .favoritesCount(artwork.getFavoritesCount())
                .commentsCount(artwork.getCommentsCount())
                .validationScore(artwork.getValidationScore())
                .likesCount(artwork.getLikesCount())
                .lovesCount(artwork.getLovesCount())
                .wowsCount(artwork.getWowsCount())
                .clapsCount(artwork.getClapsCount())
                .userReaction(userReaction)
                .status(artwork.getStatus())
                .isActive(artwork.getIsActive())
                .isFavorited(isFavorited)
                .createdAt(artwork.getCreatedAt())
                .updatedAt(artwork.getUpdatedAt())
                .primaryPhotoUrl(primaryPhotoUrl)
                .photos(photos.stream()
                        .map(photo -> ArtworkResponse.PhotoResponse.builder()
                                .id(photo.getId())
                                .photoUrl(buildFullImageUrl(photo.getPhotoUrl()))
                                .isPrimary(photo.getIsPrimary())
                                .orderIndex(photo.getOrderIndex())
                                .build())
                        .collect(Collectors.toList()))
                .recentComments(commentResponses)
                .build();
    }

    private CommentResponse mapCommentToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .artworkId(comment.getArtwork().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userFullName(comment.getUser().getFullName())
                .userPhotoUrl(buildFullImageUrl(comment.getUser().getProfilePhotoUrl()))
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
