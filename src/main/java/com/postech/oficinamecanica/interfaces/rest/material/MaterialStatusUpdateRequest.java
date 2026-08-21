package com.postech.oficinamecanica.interfaces.rest.material;

import io.swagger.v3.oas.annotations.media.Schema;

public record MaterialStatusUpdateRequest(
    @Schema(description = "Novo status desejado para o material", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status
) {}
