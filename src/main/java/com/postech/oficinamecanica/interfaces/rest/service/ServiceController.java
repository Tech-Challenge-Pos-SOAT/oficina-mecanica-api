package com.postech.oficinamecanica.interfaces.rest.service;

import com.postech.oficinamecanica.application.service.ChangeServiceStatusUseCase;
import com.postech.oficinamecanica.application.service.CreateServiceUseCase;
import com.postech.oficinamecanica.application.service.FindServiceByIdUseCase;
import com.postech.oficinamecanica.application.service.ListServicesUseCase;
import com.postech.oficinamecanica.application.service.UpdateServiceUseCase;
import com.postech.oficinamecanica.domain.service.Service;
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
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@Tag(name = "Services", description = "Catálogo de serviços da oficina")
public class ServiceController {
    private final CreateServiceUseCase createServiceUseCase;
    private final UpdateServiceUseCase updateServiceUseCase;
    private final FindServiceByIdUseCase findServiceByIdUseCase;
    private final ListServicesUseCase listServicesUseCase;
    private final ChangeServiceStatusUseCase changeServiceStatusUseCase;
    private final ServiceRestMapper mapper;

    public ServiceController(CreateServiceUseCase createServiceUseCase,
                             UpdateServiceUseCase updateServiceUseCase,
                             FindServiceByIdUseCase findServiceByIdUseCase,
                             ListServicesUseCase listServicesUseCase,
                             ChangeServiceStatusUseCase changeServiceStatusUseCase,
                             ServiceRestMapper mapper) {
        this.createServiceUseCase = createServiceUseCase;
        this.updateServiceUseCase = updateServiceUseCase;
        this.findServiceByIdUseCase = findServiceByIdUseCase;
        this.listServicesUseCase = listServicesUseCase;
        this.changeServiceStatusUseCase = changeServiceStatusUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar serviço",
        description = "Nome único no sistema. Preço deve ser maior que zero. Serviço nasce com status ACTIVE."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Serviço criado",
            content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Preço inválido (nulo, zero ou negativo)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Nome já cadastrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest request) {
        Service service = createServiceUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(service));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar serviço",
        description = "Atualiza nome, descrição e preço. Alterações de preço não afetam ordens de serviço já emitidas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Serviço atualizado",
            content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Preço inválido (nulo, zero ou negativo)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Nome já cadastrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceResponse> update(
        @Parameter(description = "ID do serviço", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateServiceRequest request
    ) {
        Service service = updateServiceUseCase.execute(id, mapper.toCommand(request));
        return ResponseEntity.ok(mapper.toResponse(service));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Serviço encontrado",
            content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceResponse> findById(
        @Parameter(description = "ID do serviço", example = "1") @PathVariable Long id
    ) {
        Service service = findServiceByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(service));
    }

    @GetMapping
    @Operation(summary = "Listar serviços", description = "Retorna todos os serviços cadastrados, ordenados por ID ascendente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de serviços recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceResponse.class))))
    })
    public List<ServiceResponse> list() {
        return listServicesUseCase.execute()
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Ativar ou inativar serviço",
        description = "Serviço inativo não pode ser incluído em uma ordem de serviço."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado",
            content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Serviço já está no status solicitado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceResponse> changeStatus(
        @Parameter(description = "ID do serviço", example = "1") @PathVariable Long id,
        @Valid @RequestBody ChangeServiceStatusRequest request
    ) {
        Service service = changeServiceStatusUseCase.execute(id, mapper.toCommand(request));
        return ResponseEntity.ok(mapper.toResponse(service));
    }
}
