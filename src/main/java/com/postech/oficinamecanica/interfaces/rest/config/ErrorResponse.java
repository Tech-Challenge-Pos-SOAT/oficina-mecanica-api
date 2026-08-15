package com.postech.oficinamecanica.interfaces.rest.config;

import java.time.Instant;

public record ErrorResponse(
    String code,
    String message,
    int status,
    Instant timestamp
) {
    public ErrorResponse(String code, String message, int status) {
        this(code, message, status, Instant.now());
    }
}
