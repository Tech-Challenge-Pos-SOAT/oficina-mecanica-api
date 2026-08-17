package com.postech.oficinamecanica.interfaces.rest.config;

import com.postech.oficinamecanica.domain.service.DuplicateServiceNameException;
import com.postech.oficinamecanica.domain.service.InvalidServicePriceException;
import com.postech.oficinamecanica.domain.service.ServiceAlreadyActiveException;
import com.postech.oficinamecanica.domain.service.ServiceAlreadyInactiveException;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Dados inválidos");
        return respond("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(IllegalArgumentException e) {
        return respond("INVALID_STATUS", "Status deve ser ACTIVE ou INACTIVE", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidServicePriceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidServicePrice(InvalidServicePriceException e) {
        return respond("INVALID_SERVICE_PRICE", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateServiceNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateServiceName(DuplicateServiceNameException e) {
        return respond("DUPLICATE_SERVICE_NAME", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleServiceNotFound(ServiceNotFoundException e) {
        return respond("SERVICE_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ServiceAlreadyActiveException.class, ServiceAlreadyInactiveException.class})
    public ResponseEntity<ErrorResponse> handleServiceStatusConflict(RuntimeException e) {
        return respond("SERVICE_STATUS_CONFLICT", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return respond("INTERNAL_ERROR", "Erro interno do servidor", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> respond(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, status.value()));
    }
}
