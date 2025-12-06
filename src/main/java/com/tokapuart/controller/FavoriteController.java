package com.tokapuart.controller;

import com.tokapuart.dto.ApiResponse;
import com.tokapuart.dto.ArtworkResponse;
import com.tokapuart.model.User;
import com.tokapuart.repository.UserRepository;
import com.tokapuart.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArtworkResponse>>> getFavorites(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            List<ArtworkResponse> favorites = favoriteService.getFavoritesByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("Favoritos obtenidos", favorites));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{artworkId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @PathVariable Long artworkId,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            favoriteService.addFavorite(artworkId, userId);
            return ResponseEntity.ok(ApiResponse.success("Agregado a favoritos", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{artworkId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long artworkId,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            favoriteService.removeFavorite(artworkId, userId);
            return ResponseEntity.ok(ApiResponse.success("Removido de favoritos", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{artworkId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @PathVariable Long artworkId,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.success("No autenticado", false));
            }

            Boolean isFavorite = favoriteService.isFavorite(artworkId, userId);
            return ResponseEntity.ok(ApiResponse.success("Estado verificado", isFavorite));
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
