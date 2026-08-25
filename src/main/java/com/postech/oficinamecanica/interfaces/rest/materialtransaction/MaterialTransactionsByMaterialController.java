package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import com.postech.oficinamecanica.application.materialtransaction.ListMaterialTransactionsByMaterialUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/materials/{materialId}/transactions")
@Tag(name = "Materials", description = "Catálogo de materiais e saldo de estoque")
public class MaterialTransactionsByMaterialController {
    private final ListMaterialTransactionsByMaterialUseCase listByMaterialUseCase;
    private final MaterialTransactionRestMapper mapper;

    public MaterialTransactionsByMaterialController(
        ListMaterialTransactionsByMaterialUseCase listByMaterialUseCase,
        MaterialTransactionRestMapper mapper
    ) {
        this.listByMaterialUseCase = listByMaterialUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(
        summary = "Listar movimentações de estoque de um material",
        description = "Retorna todas as transações de estoque de um material específico, ordenadas por ID ascendente. Pode filtrar por tipo (IN/OUT)."
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
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Material não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public List<MaterialTransactionResponse> list(
        @Parameter(description = "ID do material", example = "1")
        @PathVariable Long materialId,
        @Parameter(
            description = "Filtro por tipo: IN (entrada) ou OUT (saída). Insensível a maiúsculas. Se omitido, retorna todas.",
            example = "OUT"
        )
        @RequestParam(required = false) TransactionType type
    ) {
        return listByMaterialUseCase.execute(materialId, type)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
