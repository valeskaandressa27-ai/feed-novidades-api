package com.feednovidades.loja;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LojaRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 120, message = "deve ter no máximo 120 caracteres")
        String nome) {
}
