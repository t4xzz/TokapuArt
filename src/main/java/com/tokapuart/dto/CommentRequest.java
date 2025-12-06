package com.tokapuart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotBlank(message = "Comentario no puede estar vacío")
    @Size(min = 1, max = 500, message = "Comentario debe tener entre 1 y 500 caracteres")
    private String commentText;
}
