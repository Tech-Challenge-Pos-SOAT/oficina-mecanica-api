package com.postech.oficinamecanica.interfaces.rest.employee;

import com.postech.oficinamecanica.application.employee.CreateEmployeeUseCase;
import com.postech.oficinamecanica.application.employee.FindEmployeeByIdUseCase;
import com.postech.oficinamecanica.application.employee.ListEmployeesUseCase;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeStatusUseCase;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeUseCase;
import com.postech.oficinamecanica.domain.employee.Employee;
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
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Cadastro e gestão de funcionários")
public class EmployeeController {
    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final UpdateEmployeeUseCase updateEmployeeUseCase;
    private final UpdateEmployeeStatusUseCase updateEmployeeStatusUseCase;
    private final FindEmployeeByIdUseCase findEmployeeByIdUseCase;
    private final ListEmployeesUseCase listEmployeesUseCase;
    private final EmployeeRestMapper mapper;

    public EmployeeController(CreateEmployeeUseCase createEmployeeUseCase,
                               UpdateEmployeeUseCase updateEmployeeUseCase,
                               UpdateEmployeeStatusUseCase updateEmployeeStatusUseCase,
                               FindEmployeeByIdUseCase findEmployeeByIdUseCase,
                               ListEmployeesUseCase listEmployeesUseCase,
                               EmployeeRestMapper mapper) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.updateEmployeeStatusUseCase = updateEmployeeStatusUseCase;
        this.findEmployeeByIdUseCase = findEmployeeByIdUseCase;
        this.listEmployeesUseCase = listEmployeesUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(
        summary = "Cadastra funcionário",
        description = "Email é obrigatório e único no sistema. Senha é armazenada como hash bcrypt e nunca é retornada. Cargo deve ser MECHANIC ou ATTENDANT."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Funcionário criado com sucesso",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou cargo fora de MECHANIC/ATTENDANT",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado para outro funcionário",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = createEmployeeUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(employee));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualiza dados de um funcionário",
        description = "Atualiza nome, email e cargo. Senha não é alterada por este endpoint."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou cargo fora de MECHANIC/ATTENDANT",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado para outro funcionário",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> update(
        @Parameter(description = "ID do funcionário", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        Employee employee = updateEmployeeUseCase.execute(mapper.toCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(employee));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um funcionário pelo id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Funcionário encontrado",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> findById(
        @Parameter(description = "ID do funcionário", example = "1") @PathVariable Long id
    ) {
        Employee employee = findEmployeeByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(employee));
    }

    @GetMapping
    @Operation(summary = "Lista todos os funcionários")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de funcionários recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployeeResponse.class))))
    })
    public List<EmployeeResponse> list() {
        return listEmployeesUseCase.execute()
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Ativa ou inativa um funcionário",
        description = "Funcionário nunca é excluído, apenas ativado/inativado. Status deve ser ACTIVE ou INACTIVE."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Funcionário já está no status solicitado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> updateStatus(
        @Parameter(description = "ID do funcionário", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateEmployeeStatusRequest request
    ) {
        Employee employee = updateEmployeeStatusUseCase.execute(id, request.status());
        return ResponseEntity.ok(mapper.toResponse(employee));
    }
}
