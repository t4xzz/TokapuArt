package com.tokapuart.repository;

import com.tokapuart.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Long> {

    List<Validation> findByArtworkId(Long artworkId);

    List<Validation> findByUserId(Long userId);

    Optional<Validation> findByUserIdAndArtworkId(Long userId, Long artworkId);

    Boolean existsByUserIdAndArtworkId(Long userId, Long artworkId);

    @Query("SELECT COUNT(v) FROM Validation v WHERE v.artwork.id = :artworkId AND v.validationType = 'EXISTS'")
    Long countExistsValidationsByArtworkId(Long artworkId);

    @Query("SELECT COUNT(v) FROM Validation v WHERE v.artwork.id = :artworkId AND v.validationType IN ('NOT_EXISTS', 'INCORRECT_INFO', 'DUPLICATE')")
    Long countProblemReportsByArtworkId(Long artworkId);
}
