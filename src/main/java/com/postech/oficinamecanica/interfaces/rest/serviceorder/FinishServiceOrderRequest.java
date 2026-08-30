package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FinishServiceOrderRequest(
    @Schema(description = "Funcionario responsavel pela finalizacao", example = "1")
    @NotNull Long employeeId,

    @Schema(description = "Motivo ou observacao do encerramento", example = "Servicos concluidos e testados")
    @Size(max = 500) String observation
) {}
