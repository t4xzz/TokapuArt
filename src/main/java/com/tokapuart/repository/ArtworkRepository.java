package com.tokapuart.repository;

import com.tokapuart.model.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    @Query("SELECT a FROM Artwork a JOIN FETCH a.user WHERE a.id = :id")
    Optional<Artwork> findById(@Param("id") Long id);

    @Query("SELECT a FROM Artwork a JOIN FETCH a.user WHERE a.user.id = :userId")
    List<Artwork> findByUserId(@Param("userId") Long userId);

    List<Artwork> findByCity(String city);

    List<Artwork> findByArtworkType(Artwork.ArtworkType artworkType);

    List<Artwork> findByStatus(Artwork.ArtworkStatus status);

    @Query("SELECT a FROM Artwork a JOIN FETCH a.user WHERE a.status = 'APPROVED' ORDER BY a.createdAt DESC")
    List<Artwork> findAllApproved();

    @Query("SELECT a FROM Artwork a JOIN FETCH a.user WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.artistName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Artwork> searchArtworks(@Param("query") String query);

    // Búsqueda por proximidad (simple)
    @Query("SELECT a FROM Artwork a JOIN FETCH a.user WHERE " +
           "a.latitude BETWEEN :minLat AND :maxLat AND " +
           "a.longitude BETWEEN :minLng AND :maxLng AND " +
           "a.status = 'APPROVED'")
    List<Artwork> findNearby(
        @Param("minLat") BigDecimal minLat,
        @Param("maxLat") BigDecimal maxLat,
        @Param("minLng") BigDecimal minLng,
        @Param("maxLng") BigDecimal maxLng
    );

    @Query("SELECT a FROM Artwork a JOIN FETCH a.user WHERE a.artworkType = :type AND a.status = 'APPROVED'")
    List<Artwork> findByArtworkTypeAndApproved(@Param("type") Artwork.ArtworkType type);
}
