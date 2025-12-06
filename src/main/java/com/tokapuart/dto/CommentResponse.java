package com.tokapuart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long artworkId;
    private Long userId;
    private String username;
    private String userFullName;
    private String userPhotoUrl;
    private String commentText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
