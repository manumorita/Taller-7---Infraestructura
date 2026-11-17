package com.univalle.taller7.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 100) @Schema(example = "Juan Perez") String nombre,
        @NotBlank @Email @Size(max = 254) @Schema(example = "juan@example.com") String email) {
}
