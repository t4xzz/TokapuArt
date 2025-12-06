package com.tokapuart.repository;

import com.tokapuart.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndArtworkId(Long userId, Long artworkId);

    List<Favorite> findByUserId(Long userId);

    List<Favorite> findByArtworkId(Long artworkId);

    Boolean existsByUserIdAndArtworkId(Long userId, Long artworkId);

    void deleteByUserIdAndArtworkId(Long userId, Long artworkId);

    @Query("SELECT f.artwork.id FROM Favorite f WHERE f.user.id = :userId")
    List<Long> findArtworkIdsByUserId(Long userId);
}
