package com.postech.oficinamecanica.interfaces.rest.customer;

import com.postech.oficinamecanica.application.customer.ChangeCustomerStatusUseCase;
import com.postech.oficinamecanica.application.customer.CreateCustomerUseCase;
import com.postech.oficinamecanica.application.customer.GetCustomerUseCase;
import com.postech.oficinamecanica.application.customer.ListCustomersUseCase;
import com.postech.oficinamecanica.application.customer.UpdateCustomerUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
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
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Gestão de clientes")
public class CustomerController {
    private final CreateCustomerUseCase createCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final ChangeCustomerStatusUseCase changeCustomerStatusUseCase;
    private final CustomerRestMapper mapper;

    public CustomerController(
        CreateCustomerUseCase createCustomerUseCase,
        UpdateCustomerUseCase updateCustomerUseCase,
        GetCustomerUseCase getCustomerUseCase,
        ListCustomersUseCase listCustomersUseCase,
        ChangeCustomerStatusUseCase changeCustomerStatusUseCase,
        CustomerRestMapper mapper
    ) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.getCustomerUseCase = getCustomerUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.changeCustomerStatusUseCase = changeCustomerStatusUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(
        summary = "Cria cliente",
        description = "CPF ou CNPJ único no sistema (formatado ou não). Email obrigatório e único."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cliente criado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos (documento inválido ou campo obrigatório ausente)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Documento ou email já cadastrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = createCustomerUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(customer));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualiza cliente",
        description = "Atualiza nome, telefone e email. Documento não é alterável. Email deve permanecer único."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente atualizado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado por outro cliente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerResponse> update(
        @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateCustomerRequest request
    ) {
        Customer customer = updateCustomerUseCase.execute(mapper.toCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(customer));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta cliente por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerResponse> findById(
        @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id
    ) {
        Customer customer = getCustomerUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(customer));
    }

    @GetMapping
    @Operation(
        summary = "Listar clientes por status",
        description = "Retorna todos os clientes filtrados por status (ACTIVE/INACTIVE), ordenados por ID ascendente. Padrão: ACTIVE se não informado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clientes recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public List<CustomerResponse> list(
        @Parameter(
            description = "Filtro de status: ACTIVE ou INACTIVE (insensível a maiúsculas)",
            example = "ACTIVE",
            required = false
        )
        @RequestParam(required = false) String status
    ) {
        return listCustomersUseCase.execute(status)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Ativa ou inativa cliente",
        description = "Cliente não pode ser excluído, apenas inativado. Reativar cliente já ativo ou inativar já inativo é rejeitado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Cliente já está no status solicitado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerResponse> changeStatus(
        @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id,
        @Valid @RequestBody ChangeCustomerStatusRequest request
    ) {
        Customer customer = changeCustomerStatusUseCase.execute(mapper.toCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(customer));
    }
}
