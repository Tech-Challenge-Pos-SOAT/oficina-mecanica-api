package com.postech.oficinamecanica.interfaces.rest.vehicle;

import com.postech.oficinamecanica.application.vehicle.ChangeVehicleStatusUseCase;
import com.postech.oficinamecanica.application.vehicle.CreateVehicleUseCase;
import com.postech.oficinamecanica.application.vehicle.GetVehicleUseCase;
import com.postech.oficinamecanica.application.vehicle.ListVehiclesUseCase;
import com.postech.oficinamecanica.application.vehicle.UpdateVehicleUseCase;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.interfaces.rest.config.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Gestão de veículos")
public class VehicleController {
    private final CreateVehicleUseCase createVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final GetVehicleUseCase getVehicleUseCase;
    private final ListVehiclesUseCase listVehiclesUseCase;
    private final ChangeVehicleStatusUseCase changeVehicleStatusUseCase;
    private final VehicleRestMapper mapper;

    public VehicleController(
        CreateVehicleUseCase createVehicleUseCase,
        UpdateVehicleUseCase updateVehicleUseCase,
        GetVehicleUseCase getVehicleUseCase,
        ListVehiclesUseCase listVehiclesUseCase,
        ChangeVehicleStatusUseCase changeVehicleStatusUseCase,
        VehicleRestMapper mapper
    ) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.updateVehicleUseCase = updateVehicleUseCase;
        this.getVehicleUseCase = getVehicleUseCase;
        this.listVehiclesUseCase = listVehiclesUseCase;
        this.changeVehicleStatusUseCase = changeVehicleStatusUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(
        summary = "Cria veículo",
        description = "Cliente vinculado deve existir e estar ativo. Placa única no sistema (formatada ou não)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Veículo criado",
            content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos (placa inválida ou campo obrigatório ausente)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Placa já cadastrada ou cliente inativo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = createVehicleUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(vehicle));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualiza veículo",
        description = "Atualiza marca, modelo, placa e ano. Cliente vinculado não é alterável. Placa deve permanecer única."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo atualizado",
            content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Placa já cadastrada por outro veículo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> update(
        @Parameter(description = "ID do veículo", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateVehicleRequest request
    ) {
        Vehicle vehicle = updateVehicleUseCase.execute(mapper.toCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta veículo por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo encontrado",
            content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> findById(
        @Parameter(description = "ID do veículo", example = "1") @PathVariable Long id
    ) {
        Vehicle vehicle = getVehicleUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }

    @GetMapping
    @Operation(
        summary = "Listar veículos por status",
        description = "Retorna todos os veículos filtrados por status (ACTIVE/INACTIVE), ordenados por ID ascendente. Padrão: ACTIVE se não informado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de veículos recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = VehicleResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public List<VehicleResponse> list(
        @Parameter(
            description = "Filtro de status: ACTIVE ou INACTIVE (insensível a maiúsculas)",
            example = "ACTIVE",
            required = false
        )
        @RequestParam(required = false) String status
    ) {
        return listVehiclesUseCase.execute(status)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Ativa ou inativa veículo",
        description = "Veículo não pode ser excluído, apenas inativado. Reativar veículo já ativo ou inativar já inativo é rejeitado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado",
            content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Veículo já está no status solicitado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> changeStatus(
        @Parameter(description = "ID do veículo", example = "1") @PathVariable Long id,
        @Valid @RequestBody ChangeVehicleStatusRequest request
    ) {
        Vehicle vehicle = changeVehicleStatusUseCase.execute(mapper.toCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }
}
