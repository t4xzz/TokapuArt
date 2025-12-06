package com.tokapuart.repository;

import com.tokapuart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    @Query("SELECT COUNT(a) FROM Artwork a WHERE a.user.id = :userId")
    Long countArtworksByUserId(Long userId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user.id = :userId")
    Long countFavoritesByUserId(Long userId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    Long countCommentsByUserId(Long userId);

    @Query("SELECT COUNT(f) FROM Follower f WHERE f.following.id = :userId")
    Long countFollowersByUserId(Long userId);

    @Query("SELECT COUNT(f) FROM Follower f WHERE f.follower.id = :userId")
    Long countFollowingByUserId(Long userId);
}
