package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record MaterialTransactionResponse(
    @Schema(description = "Identificador único da transação", example = "1")
    Long id,

    @Schema(description = "ID do material movimentado", example = "10")
    Long materialId,

    @Schema(description = "ID da ordem de serviço (nulo para entradas de estoque)", example = "5")
    Long serviceOrderId,

    @Schema(description = "Quantidade movimentada", example = "100")
    Integer quantity,

    @Schema(description = "Tipo de transação: IN (entrada) ou OUT (saída)", example = "OUT")
    String type,

    @Schema(description = "Data/hora da transação", example = "2026-08-19T10:30:00Z")
    Instant createdAt
) {}
