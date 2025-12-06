package com.tokapuart.repository;

import com.tokapuart.model.ArtworkPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworkPhotoRepository extends JpaRepository<ArtworkPhoto, Long> {

    List<ArtworkPhoto> findByArtworkIdOrderByOrderIndexAsc(Long artworkId);

    Optional<ArtworkPhoto> findByArtworkIdAndIsPrimaryTrue(Long artworkId);

    @Query("SELECT p FROM ArtworkPhoto p WHERE p.artwork.id = :artworkId ORDER BY p.createdAt DESC")
    List<ArtworkPhoto> findByArtworkIdOrderByCreatedAtDesc(Long artworkId);

    Long countByArtworkId(Long artworkId);
}
