package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OpenServiceOrderRequest(
    @Schema(description = "CPF ou CNPJ do cliente", example = "529.982.247-25")
    @NotBlank String customerDocument,

    @Schema(description = "Placa do veiculo (formato antigo ou Mercosul)", example = "ABC-1234")
    @NotBlank String plate,

    @Schema(description = "Funcionario responsavel pela abertura", example = "1")
    @NotNull Long employeeId
) {}
