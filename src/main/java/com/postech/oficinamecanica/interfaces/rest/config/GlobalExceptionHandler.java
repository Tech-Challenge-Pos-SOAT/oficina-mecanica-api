package com.postech.oficinamecanica.interfaces.rest.config;

import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
import com.postech.oficinamecanica.domain.shared.exceptions.InvalidParametersException;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse(
            "INVALID_STATUS",
            "Status deve ser ACTIVE ou INACTIVE",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException e) {
        ErrorResponse error = new ErrorResponse(
            "RESOURCE_NOT_FOUND",
            e.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidParametersException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameters(InvalidParametersException e) {
        ErrorResponse error = new ErrorResponse(
            "INVALID_PARAMETERS",
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(BusinessRuleViolationException e) {
        ErrorResponse error = new ErrorResponse(
            "BUSINESS_RULE_VIOLATION",
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", errorMessage, HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "Erro interno do servidor",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
