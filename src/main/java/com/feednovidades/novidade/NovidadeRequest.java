package com.feednovidades.novidade;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovidadeRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 500, message = "deve ter no máximo 500 caracteres")
        String texto,

        @DecimalMin(value = "0.0", inclusive = false, message = "deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "deve ter no máximo 8 dígitos inteiros e 2 decimais")
        BigDecimal preco) {
}
