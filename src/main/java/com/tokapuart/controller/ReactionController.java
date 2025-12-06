package com.tokapuart.controller;

import com.tokapuart.dto.ApiResponse;
import com.tokapuart.model.Artwork;
import com.tokapuart.model.ArtworkReaction;
import com.tokapuart.model.User;
import com.tokapuart.repository.ArtworkReactionRepository;
import com.tokapuart.repository.ArtworkRepository;
import com.tokapuart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/artworks/{artworkId}/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ArtworkReactionRepository reactionRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<String>> addOrUpdateReaction(
            @PathVariable Long artworkId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            String reactionTypeStr = request.get("reactionType");
            if (reactionTypeStr == null || reactionTypeStr.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tipo de reacción requerido"));
            }

            ArtworkReaction.ReactionType reactionType;
            try {
                reactionType = ArtworkReaction.ReactionType.valueOf(reactionTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tipo de reacción inválido: " + reactionTypeStr));
            }

            Artwork artwork = artworkRepository.findById(artworkId)
                    .orElseThrow(() -> new RuntimeException("Obra no encontrada"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar si ya existe una reacción del usuario
            Optional<ArtworkReaction> existingReaction =
                    reactionRepository.findByUserIdAndArtworkId(userId, artworkId);

            if (existingReaction.isPresent()) {
                // Actualizar reacción existente
                ArtworkReaction reaction = existingReaction.get();
                ArtworkReaction.ReactionType oldType = reaction.getReactionType();

                // Decrementar contador anterior
                decrementReactionCounter(artwork, oldType);

                // Actualizar tipo de reacción
                reaction.setReactionType(reactionType);
                reactionRepository.save(reaction);

                // Incrementar nuevo contador
                incrementReactionCounter(artwork, reactionType);
            } else {
                // Crear nueva reacción
                ArtworkReaction reaction = ArtworkReaction.builder()
                        .artwork(artwork)
                        .user(user)
                        .reactionType(reactionType)
                        .build();
                reactionRepository.save(reaction);

                // Incrementar contador
                incrementReactionCounter(artwork, reactionType);
            }

            artworkRepository.save(artwork);

            return ResponseEntity.ok(
                    ApiResponse.success("Reacción agregada exitosamente", reactionType.name()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al agregar reacción: " + e.getMessage()));
        }
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<ApiResponse<Void>> removeReaction(
            @PathVariable Long artworkId,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Debes iniciar sesión"));
            }

            Optional<ArtworkReaction> reaction =
                    reactionRepository.findByUserIdAndArtworkId(userId, artworkId);

            if (reaction.isPresent()) {
                Artwork artwork = artworkRepository.findById(artworkId)
                        .orElseThrow(() -> new RuntimeException("Obra no encontrada"));

                // Decrementar contador
                decrementReactionCounter(artwork, reaction.get().getReactionType());
                artworkRepository.save(artwork);

                // Eliminar reacción
                reactionRepository.delete(reaction.get());

                return ResponseEntity.ok(ApiResponse.success("Reacción eliminada exitosamente", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No se encontró reacción para eliminar"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al eliminar reacción: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getUserReaction(
            @PathVariable Long artworkId,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.success("Sin reacción", null));
            }

            Optional<ArtworkReaction> reaction =
                    reactionRepository.findByUserIdAndArtworkId(userId, artworkId);

            String reactionType = reaction.map(r -> r.getReactionType().name()).orElse(null);
            return ResponseEntity.ok(ApiResponse.success("Reacción obtenida", reactionType));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener reacción: " + e.getMessage()));
        }
    }

    private void incrementReactionCounter(Artwork artwork, ArtworkReaction.ReactionType type) {
        switch (type) {
            case LIKE:
                artwork.setLikesCount((artwork.getLikesCount() != null ? artwork.getLikesCount() : 0) + 1);
                break;
            case LOVE:
                artwork.setLovesCount((artwork.getLovesCount() != null ? artwork.getLovesCount() : 0) + 1);
                break;
            case WOW:
                artwork.setWowsCount((artwork.getWowsCount() != null ? artwork.getWowsCount() : 0) + 1);
                break;
            case CLAP:
                artwork.setClapsCount((artwork.getClapsCount() != null ? artwork.getClapsCount() : 0) + 1);
                break;
        }
    }

    private void decrementReactionCounter(Artwork artwork, ArtworkReaction.ReactionType type) {
        switch (type) {
            case LIKE:
                artwork.setLikesCount(Math.max(0, (artwork.getLikesCount() != null ? artwork.getLikesCount() : 0) - 1));
                break;
            case LOVE:
                artwork.setLovesCount(Math.max(0, (artwork.getLovesCount() != null ? artwork.getLovesCount() : 0) - 1));
                break;
            case WOW:
                artwork.setWowsCount(Math.max(0, (artwork.getWowsCount() != null ? artwork.getWowsCount() : 0) - 1));
                break;
            case CLAP:
                artwork.setClapsCount(Math.max(0, (artwork.getClapsCount() != null ? artwork.getClapsCount() : 0) - 1));
                break;
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
