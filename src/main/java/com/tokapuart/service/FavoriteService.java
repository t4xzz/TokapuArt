package com.tokapuart.service;

import com.tokapuart.dto.ArtworkResponse;
import com.tokapuart.model.Artwork;
import com.tokapuart.model.Favorite;
import com.tokapuart.model.User;
import com.tokapuart.repository.ArtworkRepository;
import com.tokapuart.repository.FavoriteRepository;
import com.tokapuart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;
    private final ArtworkService artworkService;

    @Transactional
    public void addFavorite(Long artworkId, Long userId) {
        if (favoriteRepository.existsByUserIdAndArtworkId(userId, artworkId)) {
            throw new RuntimeException("Esta obra ya está en tus favoritos");
        }

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Favorite favorite = Favorite.builder()
                .artwork(artwork)
                .user(user)
                .build();

        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long artworkId, Long userId) {
        if (!favoriteRepository.existsByUserIdAndArtworkId(userId, artworkId)) {
            throw new RuntimeException("Esta obra no está en tus favoritos");
        }

        favoriteRepository.deleteByUserIdAndArtworkId(userId, artworkId);
    }

    @Transactional(readOnly = true)
    public List<ArtworkResponse> getFavoritesByUserId(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);

        return favorites.stream()
                .map(favorite -> artworkService.getArtworkById(favorite.getArtwork().getId(), userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Boolean isFavorite(Long artworkId, Long userId) {
        return favoriteRepository.existsByUserIdAndArtworkId(userId, artworkId);
    }
}
