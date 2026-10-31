package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OpenServiceOrderRequest(
    @Schema(description = "CPF ou CNPJ do cliente", example = "529.982.247-25")
    @NotBlank String customerDocument,

    @Schema(description = "Placa do veiculo (formato antigo ou Mercosul)", example = "ABC-1234")
    @NotBlank String plate,

    @Schema(description = "Funcionario responsavel pela abertura", example = "1")
    @NotNull Long employeeId,

    @Schema(description = "Ids de servicos do catalogo ja incluidos na abertura (opcional)", example = "[1, 2]")
    List<@NotNull Long> serviceIds,

    @Schema(description = "Pecas/insumos ja incluidos na abertura (opcional)")
    List<@Valid @NotNull MaterialItem> materials
) {
    public record MaterialItem(
        @Schema(description = "Id do material no catalogo", example = "1")
        @NotNull Long materialId,

        @Schema(description = "Quantidade (maior que zero)", example = "2")
        @NotNull @Positive Integer quantity
    ) {}
}
