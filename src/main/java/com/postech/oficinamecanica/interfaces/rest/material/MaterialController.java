package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.application.material.ChangeMaterialStatusUseCase;
import com.postech.oficinamecanica.application.material.GetMaterialUseCase;
import com.postech.oficinamecanica.application.material.ListMaterialsUseCase;
import com.postech.oficinamecanica.application.material.ListLowStockMaterialsUseCase;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
@Tag(name = "Materials", description = "Catálogo de materiais e saldo de estoque")
public class MaterialController {
    private final ListMaterialsUseCase listMaterialsUseCase;
    private final GetMaterialUseCase getMaterialUseCase;
    private final ListLowStockMaterialsUseCase listLowStockUseCase;
    private final ChangeMaterialStatusUseCase changeMaterialStatusUseCase;
    private final MaterialRestMapper mapper;

    public MaterialController(ListMaterialsUseCase listMaterialsUseCase, GetMaterialUseCase getMaterialUseCase, ListLowStockMaterialsUseCase listLowStockUseCase, ChangeMaterialStatusUseCase changeMaterialStatusUseCase, MaterialRestMapper mapper) {
        this.listMaterialsUseCase = listMaterialsUseCase;
        this.getMaterialUseCase = getMaterialUseCase;
        this.listLowStockUseCase = listLowStockUseCase;
        this.changeMaterialStatusUseCase = changeMaterialStatusUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(
        summary = "Listar materiais por status",
        description = "Retorna os materiais filtrados por status (ACTIVE/INACTIVE), ordenados por ID ascendente. Padrão: ACTIVE se não informado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de materiais recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MaterialResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public List<MaterialResponse> list(
        @Parameter(
            description = "Filtro de status: ACTIVE ou INACTIVE (insensível a maiúsculas)",
            example = "ACTIVE"
        )
        @RequestParam(required = false) String status
    ) {
        return listMaterialsUseCase.execute(status)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GetMapping("/low-stock")
    @Operation(
        summary = "Listar materiais com estoque crítico",
        description = "Retorna materiais com saldo atual abaixo do estoque mínimo, filtrados por status."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de materiais com estoque crítico recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MaterialResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public List<MaterialResponse> listLowStock(
        @Parameter(description = "Filtro de status", example = "ACTIVE")
        @RequestParam(required = false) String status
    ) {
        return listLowStockUseCase.execute(status)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar material por ID",
        description = "Retorna os detalhes completos de um único material a partir de seu ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Material encontrado com sucesso",
            content = @Content(schema = @Schema(implementation = MaterialResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Material não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public MaterialResponse getById(
        @Parameter(description = "ID do material a ser buscado", example = "1")
        @PathVariable Long id
    ) {
        return mapper.toResponse(getMaterialUseCase.execute(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do material", description = "Muda o status do material para ACTIVE ou INACTIVE")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Status do material atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = MaterialResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Status inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Material não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public MaterialResponse updateStatus(
        @Parameter(description = "ID do Material") @PathVariable Long id,
        @RequestBody MaterialStatusUpdateRequest request
    ) {
        var updated = changeMaterialStatusUseCase.execute(id, request.status());
        return mapper.toResponse(updated);
    }
}
