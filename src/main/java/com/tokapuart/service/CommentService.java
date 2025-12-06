package com.tokapuart.service;

import com.tokapuart.dto.CommentRequest;
import com.tokapuart.dto.CommentResponse;
import com.tokapuart.model.Artwork;
import com.tokapuart.model.Comment;
import com.tokapuart.model.User;
import com.tokapuart.repository.ArtworkRepository;
import com.tokapuart.repository.CommentRepository;
import com.tokapuart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse addComment(Long artworkId, CommentRequest request, Long userId) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Comment comment = Comment.builder()
                .artwork(artwork)
                .user(user)
                .commentText(request.getCommentText())
                .build();

        comment = commentRepository.save(comment);

        return mapToResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByArtworkId(Long artworkId) {
        return commentRepository.findByArtworkIdOrderByCreatedAtDesc(artworkId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .artworkId(comment.getArtwork().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userFullName(comment.getUser().getFullName())
                .userPhotoUrl(comment.getUser().getProfilePhotoUrl())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
