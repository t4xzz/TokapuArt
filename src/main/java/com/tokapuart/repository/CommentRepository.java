package com.tokapuart.repository;

import com.tokapuart.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.artwork.id = :artworkId ORDER BY c.createdAt DESC")
    List<Comment> findByArtworkIdOrderByCreatedAtDesc(Long artworkId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.artwork.id = :artworkId ORDER BY c.createdAt DESC")
    List<Comment> findTop3ByArtworkIdOrderByCreatedAtDesc(Long artworkId);

    Long countByArtworkId(Long artworkId);

    Long countByUserId(Long userId);
}
