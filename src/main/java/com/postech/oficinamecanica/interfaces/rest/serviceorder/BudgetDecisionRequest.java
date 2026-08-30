package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BudgetDecisionRequest(
    @Schema(description = "CPF ou CNPJ do cliente, usado como prova de posse da ordem", example = "529.982.247-25")
    @NotBlank String customerDocument,

    @Schema(description = "true aprova o orcamento, false recusa", example = "true")
    @NotNull Boolean approved,

    @Schema(description = "Motivo da recusa", example = "Vou fazer em outro lugar")
    @Size(max = 500) String reason
) {}
