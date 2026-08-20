package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import com.postech.oficinamecanica.application.materialtransaction.ListMaterialTransactionsUseCase;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.interfaces.rest.config.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/material-transactions")
@Validated
@Tag(name = "Material Transactions", description = "Movimentações de estoque de materiais")
public class MaterialTransactionController {
    private final ListMaterialTransactionsUseCase listMaterialTransactionsUseCase;
    private final MaterialTransactionRestMapper mapper;

    public MaterialTransactionController(
        ListMaterialTransactionsUseCase listMaterialTransactionsUseCase,
        MaterialTransactionRestMapper mapper
    ) {
        this.listMaterialTransactionsUseCase = listMaterialTransactionsUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(
        summary = "Listar movimentações de estoque",
        description = "Retorna todas as transações de materiais ordenadas por ID ascendente. Pode filtrar por tipo (IN/OUT)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de transações recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MaterialTransactionResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Tipo de transação inválido (deve ser IN ou OUT)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public List<MaterialTransactionResponse> list(
        @Parameter(
            description = "Filtro por tipo: IN (entrada) ou OUT (saída). Insensível a maiúsculas. Se omitido, retorna todas.",
            example = "OUT"
        )
        @RequestParam(required = false) TransactionType type
    ) {
        return listMaterialTransactionsUseCase.execute(type)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }
}
