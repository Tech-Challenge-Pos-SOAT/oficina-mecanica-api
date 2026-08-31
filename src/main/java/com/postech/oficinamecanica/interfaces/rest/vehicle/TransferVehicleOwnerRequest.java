package com.postech.oficinamecanica.interfaces.rest.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferVehicleOwnerRequest(
    @Schema(description = "CPF ou CNPJ do novo dono, que ja deve estar cadastrado e ativo",
            example = "111.444.777-35")
    @NotBlank String newOwnerDocument,

    @Schema(description = "Funcionario responsavel pela transferencia", example = "1")
    @NotNull Long employeeId
) {}
