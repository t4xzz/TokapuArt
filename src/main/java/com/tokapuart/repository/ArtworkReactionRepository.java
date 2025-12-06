package com.tokapuart.repository;

import com.tokapuart.model.ArtworkReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtworkReactionRepository extends JpaRepository<ArtworkReaction, Long> {

    Optional<ArtworkReaction> findByUserIdAndArtworkId(Long userId, Long artworkId);

    Boolean existsByUserIdAndArtworkId(Long userId, Long artworkId);

    void deleteByUserIdAndArtworkId(Long userId, Long artworkId);

    @Query("SELECT COUNT(r) FROM ArtworkReaction r WHERE r.artwork.id = :artworkId AND r.reactionType = 'LIKE'")
    Long countLikesByArtworkId(Long artworkId);

    @Query("SELECT COUNT(r) FROM ArtworkReaction r WHERE r.artwork.id = :artworkId AND r.reactionType = 'LOVE'")
    Long countLovesByArtworkId(Long artworkId);

    @Query("SELECT COUNT(r) FROM ArtworkReaction r WHERE r.artwork.id = :artworkId AND r.reactionType = 'WOW'")
    Long countWowsByArtworkId(Long artworkId);

    @Query("SELECT COUNT(r) FROM ArtworkReaction r WHERE r.artwork.id = :artworkId AND r.reactionType = 'CLAP'")
    Long countClapsByArtworkId(Long artworkId);
}
