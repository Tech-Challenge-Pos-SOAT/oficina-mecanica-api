package com.postech.oficinamecanica.interfaces.rest.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateEmployeeRequest(
    @Schema(description = "Nome completo do funcionário", example = "Carlos Souza")
    @NotBlank String name,

    @Schema(description = "Email único do funcionário", example = "carlos.souza@oficina.com")
    @NotBlank @Email String email,

    @Schema(description = "Senha em texto puro, armazenada como hash bcrypt", example = "senha123")
    @NotBlank String password,

    @Schema(description = "Cargo do funcionário", example = "MECHANIC", allowableValues = {"MECHANIC", "ATTENDANT"})
    @NotBlank String role
) {}
