package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.application.material.ListMaterialsUseCase;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
@Tag(name = "Materials", description = "Catálogo de materiais e saldo de estoque")
public class MaterialController {
    private final ListMaterialsUseCase listMaterialsUseCase;
    private final MaterialRestMapper mapper;

    public MaterialController(ListMaterialsUseCase listMaterialsUseCase, MaterialRestMapper mapper) {
        this.listMaterialsUseCase = listMaterialsUseCase;
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
            example = "ACTIVE",
            required = false
        )
        @RequestParam(required = false) String status
    ) {
        return listMaterialsUseCase.execute(status)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }
}
