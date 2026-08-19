package com.postech.oficinamecanica.interfaces.rest.config;

import com.postech.oficinamecanica.domain.customer.CustomerAlreadyActiveException;
import com.postech.oficinamecanica.domain.customer.CustomerAlreadyInactiveException;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.DuplicateDocumentException;
import com.postech.oficinamecanica.domain.customer.DuplicateEmailException;
import com.postech.oficinamecanica.domain.customer.InvalidDocumentException;
import com.postech.oficinamecanica.domain.employee.EmployeeAlreadyActiveException;
import com.postech.oficinamecanica.domain.employee.EmployeeAlreadyInactiveException;
import com.postech.oficinamecanica.domain.employee.EmployeeNotFoundException;
import com.postech.oficinamecanica.domain.employee.InvalidEmployeeRoleException;
import com.postech.oficinamecanica.domain.material.MaterialNotFoundException;
import com.postech.oficinamecanica.domain.service.DuplicateServiceNameException;
import com.postech.oficinamecanica.domain.service.InvalidServicePriceException;
import com.postech.oficinamecanica.domain.service.ServiceAlreadyActiveException;
import com.postech.oficinamecanica.domain.service.ServiceAlreadyInactiveException;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.DuplicatePlateException;
import com.postech.oficinamecanica.domain.vehicle.InvalidPlateException;
import com.postech.oficinamecanica.domain.vehicle.VehicleAlreadyActiveException;
import com.postech.oficinamecanica.domain.vehicle.VehicleAlreadyInactiveException;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
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

    // ---- Customer ----

    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidDocumentException e) {
        return respond("INVALID_DOCUMENT", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicateDocumentException e) {
        return respond("DUPLICATE_DOCUMENT", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicateEmailException e) {
        return respond("DUPLICATE_EMAIL", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(CustomerNotFoundException e) {
        return respond("CUSTOMER_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomerAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handle(CustomerAlreadyActiveException e) {
        return respond("CUSTOMER_ALREADY_ACTIVE", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomerAlreadyInactiveException.class)
    public ResponseEntity<ErrorResponse> handle(CustomerAlreadyInactiveException e) {
        return respond("CUSTOMER_ALREADY_INACTIVE", e.getMessage(), HttpStatus.CONFLICT);
    }

    // ---- Employee ----
    // Nota: domain.employee.DuplicateEmailException tem o mesmo nome simples de
    // domain.customer.DuplicateEmailException (ja importada acima), por isso aqui
    // usamos o nome totalmente qualificado em vez de um segundo import.

    @ExceptionHandler(InvalidEmployeeRoleException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidEmployeeRoleException e) {
        return respond("INVALID_ROLE", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(com.postech.oficinamecanica.domain.employee.DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handle(com.postech.oficinamecanica.domain.employee.DuplicateEmailException e) {
        return respond("DUPLICATE_EMAIL", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(EmployeeNotFoundException e) {
        return respond("EMPLOYEE_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({EmployeeAlreadyActiveException.class, EmployeeAlreadyInactiveException.class})
    public ResponseEntity<ErrorResponse> handleEmployeeStatusConflict(RuntimeException e) {
        return respond("EMPLOYEE_STATUS_CONFLICT", e.getMessage(), HttpStatus.CONFLICT);
    }

    // ---- Material ----
    @ExceptionHandler(MaterialNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(MaterialNotFoundException e) {
        return respond("MATERIAL_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // ---- Service ----

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

    // ---- Vehicle ----

    @ExceptionHandler(InvalidPlateException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidPlateException e) {
        return respond("INVALID_PLATE", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicatePlateException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicatePlateException e) {
        return respond("DUPLICATE_PLATE", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(VehicleNotFoundException e) {
        return respond("VEHICLE_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(VehicleAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handle(VehicleAlreadyActiveException e) {
        return respond("VEHICLE_ALREADY_ACTIVE", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(VehicleAlreadyInactiveException.class)
    public ResponseEntity<ErrorResponse> handle(VehicleAlreadyInactiveException e) {
        return respond("VEHICLE_ALREADY_INACTIVE", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomerNotActiveException.class)
    public ResponseEntity<ErrorResponse> handle(CustomerNotActiveException e) {
        return respond("CUSTOMER_NOT_ACTIVE", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return respond("INTERNAL_ERROR", "Erro interno do servidor", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> respond(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, status.value()));
    }
}
