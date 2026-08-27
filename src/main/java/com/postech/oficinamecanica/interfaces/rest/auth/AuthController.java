package com.postech.oficinamecanica.interfaces.rest.auth;

import com.postech.oficinamecanica.application.auth.AuthenticateEmployeeUseCase;
import com.postech.oficinamecanica.application.auth.LoginCommand;
import com.postech.oficinamecanica.application.auth.LoginResult;
import com.postech.oficinamecanica.domain.employee.Employee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login de funcionários e emissão de token JWT")
public class AuthController {
    private final AuthenticateEmployeeUseCase authenticateEmployeeUseCase;

    public AuthController(AuthenticateEmployeeUseCase authenticateEmployeeUseCase) {
        this.authenticateEmployeeUseCase = authenticateEmployeeUseCase;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Autentica um funcionário",
        description = "Recebe email e senha, valida se o funcionário existe, está ativo e a senha confere, "
            + "e retorna um token JWT para uso nas APIs administrativas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas (email inexistente, senha incorreta ou funcionário inativo)",
            content = @Content(schema = @Schema(implementation = AuthErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authenticateEmployeeUseCase.execute(new LoginCommand(request.email(), request.password()));
        Employee employee = result.employee();

        LoginResponse response = new LoginResponse(
            result.token(), "Bearer", employee.getId(), employee.getName(), employee.getRole().name()
        );

        return ResponseEntity.ok(response);
    }
}
