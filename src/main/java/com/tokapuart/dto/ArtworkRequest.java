package com.tokapuart.dto;

import com.tokapuart.model.Artwork;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtworkRequest {

    @NotBlank(message = "Título es requerido")
    @Size(min = 3, max = 200, message = "Título debe tener entre 3 y 200 caracteres")
    private String title;

    @NotBlank(message = "Descripción es requerida")
    @Size(min = 10, max = 5000, message = "Descripción debe tener entre 10 y 5000 caracteres")
    private String description;

    private String artistName;

    @NotNull(message = "Tipo de arte es requerido")
    private Artwork.ArtworkType artworkType;

    private String technique;

    @Min(value = 1900, message = "Año debe ser mayor a 1900")
    @Max(value = 2100, message = "Año debe ser menor a 2100")
    private Integer yearCreated;

    @NotNull(message = "Latitud es requerida")
    @DecimalMin(value = "-90.0", message = "Latitud debe ser mayor a -90")
    @DecimalMax(value = "90.0", message = "Latitud debe ser menor a 90")
    private BigDecimal latitude;

    @NotNull(message = "Longitud es requerida")
    @DecimalMin(value = "-180.0", message = "Longitud debe ser mayor a -180")
    @DecimalMax(value = "180.0", message = "Longitud debe ser menor a 180")
    private BigDecimal longitude;

    private String address;

    @NotBlank(message = "Ciudad es requerida")
    private String city;
}
