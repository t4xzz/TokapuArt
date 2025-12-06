package com.tokapuart.controller;

import com.tokapuart.dto.ApiResponse;
import com.tokapuart.dto.ArtworkRequest;
import com.tokapuart.dto.ArtworkResponse;
import com.tokapuart.model.Artwork;
import com.tokapuart.model.ArtworkPhoto;
import com.tokapuart.model.User;
import com.tokapuart.repository.ArtworkPhotoRepository;
import com.tokapuart.repository.ArtworkRepository;
import com.tokapuart.repository.UserRepository;
import com.tokapuart.service.ArtworkService;
import com.tokapuart.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/artworks")
@RequiredArgsConstructor
public class ArtworkController {

    private final ArtworkService artworkService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;
    private final ArtworkPhotoRepository artworkPhotoRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArtworkResponse>>> getAllArtworks(Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            List<ArtworkResponse> artworks = artworkService.getAllArtworks(currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Obras obtenidas exitosamente", artworks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener obras: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtworkResponse>> getArtworkById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            ArtworkResponse artwork = artworkService.getArtworkById(id, currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Obra obtenida exitosamente", artwork));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArtworkResponse>> createArtwork(
            @Valid @RequestBody ArtworkRequest request,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión para crear una obra"));
            }

            ArtworkResponse artwork = artworkService.createArtwork(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Obra creada exitosamente", artwork));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtworkResponse>> updateArtwork(
            @PathVariable Long id,
            @Valid @RequestBody ArtworkRequest request,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            ArtworkResponse artwork = artworkService.updateArtwork(id, request, userId);
            return ResponseEntity.ok(ApiResponse.success("Obra actualizada exitosamente", artwork));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArtwork(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            artworkService.deleteArtwork(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Obra eliminada exitosamente", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ArtworkResponse>>> searchArtworks(
            @RequestParam String query,
            Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            List<ArtworkResponse> artworks = artworkService.searchArtworks(query, currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Búsqueda completada", artworks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ArtworkResponse>>> filterByType(
            @RequestParam Artwork.ArtworkType type,
            Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            List<ArtworkResponse> artworks = artworkService.filterByType(type, currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Obras filtradas exitosamente", artworks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ArtworkResponse>>> getNearbyArtworks(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            List<ArtworkResponse> artworks = artworkService.getNearbyArtworks(
                    latitude, longitude, radiusKm, currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Obras cercanas obtenidas", artworks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<ApiResponse<String>> addPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(required = false, defaultValue = "false") Boolean isPrimary,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            Artwork artwork = artworkRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Obra no encontrada"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String photoUrl = fileStorageService.storeFile(photo);

            ArtworkPhoto artworkPhoto = ArtworkPhoto.builder()
                    .artwork(artwork)
                    .user(user)
                    .photoUrl(photoUrl)
                    .isPrimary(isPrimary)
                    .orderIndex(artworkPhotoRepository.countByArtworkId(id).intValue())
                    .build();

            artworkPhotoRepository.save(artworkPhoto);

            return ResponseEntity.ok(ApiResponse.success("Foto agregada exitosamente", photoUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ArtworkResponse>>> getArtworksByUser(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            List<ArtworkResponse> artworks = artworkService.getArtworksByUserId(userId, currentUserId);
            return ResponseEntity.ok(ApiResponse.success("Obras del usuario obtenidas", artworks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }
}
