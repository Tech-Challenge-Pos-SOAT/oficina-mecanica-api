package com.postech.oficinamecanica.interfaces.rest.customer;

import com.postech.oficinamecanica.application.customer.ListCustomersUseCase;
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
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Gestão de clientes")
public class CustomerController {
    private final ListCustomersUseCase listCustomersUseCase;
    private final CustomerRestMapper mapper;

    public CustomerController(ListCustomersUseCase listCustomersUseCase, CustomerRestMapper mapper) {
        this.listCustomersUseCase = listCustomersUseCase;
        this.mapper = mapper;
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
}
