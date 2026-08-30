package com.postech.oficinamecanica.interfaces.rest.config;

import com.postech.oficinamecanica.domain.auth.InvalidCredentialsException;
import com.postech.oficinamecanica.domain.serviceorder.InvalidServiceOrderStatusException;
import com.postech.oficinamecanica.domain.serviceorder.InvalidServiceOrderTransitionException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderAccessDeniedException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotOpenForItemsException;
import com.postech.oficinamecanica.domain.serviceorder.VehicleNotOwnedByCustomerException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
import com.postech.oficinamecanica.domain.shared.exceptions.InvalidParametersException;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
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
import com.postech.oficinamecanica.interfaces.rest.auth.AuthErrorResponse;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    // ---- Auth ----
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<AuthErrorResponse> handle(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthErrorResponse("Credenciais inválidas"));
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
  
    // ---- Service Order ----

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ServiceOrderNotFoundException e) {
        return respond("SERVICE_ORDER_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidServiceOrderTransitionException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidServiceOrderTransitionException e) {
        return respond("INVALID_STATUS_TRANSITION", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceOrderNotOpenForItemsException.class)
    public ResponseEntity<ErrorResponse> handle(ServiceOrderNotOpenForItemsException e) {
        return respond("SERVICE_ORDER_NOT_OPEN_FOR_ITEMS", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceOrderAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handle(ServiceOrderAccessDeniedException e) {
        return respond("SERVICE_ORDER_ACCESS_DENIED", e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(VehicleNotOwnedByCustomerException.class)
    public ResponseEntity<ErrorResponse> handle(VehicleNotOwnedByCustomerException e) {
        return respond("VEHICLE_NOT_OWNED_BY_CUSTOMER", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidServiceOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidServiceOrderStatusException e) {
        return respond("INVALID_SERVICE_ORDER_STATUS", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Parametros de query anotados em controller @Validated (ex.: ?document= vazio)
    // caem aqui; sem isso virariam 500 no handler generico.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handle(ConstraintViolationException e) {
        return respond("VALIDATION_ERROR", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ---- Generic Exceptions ----  
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        String paramName = ex.getName();
        Object providedValue = ex.getValue();
        Class<?> requiredType = ex.getRequiredType();

        StringBuilder message = new StringBuilder(
                String.format("O parâmetro '%s' recebeu o valor '%s', que é inválido. ", paramName, providedValue)
        );

        if (requiredType != null) {
            if (requiredType.isEnum()) {
                String acceptedValues = Arrays.stream(requiredType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                message.append(String.format("Os valores aceitos são: [%s].", acceptedValues));
            } else {
                message.append(String.format("O tipo esperado é: %s.", requiredType.getSimpleName()));
            }
        }

        ErrorResponse error = new ErrorResponse("INVALID_PARAMETER_TYPE", message.toString(), HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    // Rota inexistente cai aqui antes do tratamento padrao do Spring, porque
    // ExceptionHandlerExceptionResolver roda antes do DefaultHandlerExceptionResolver.
    // Sem estes dois handlers, qualquer URL errada em /api/* responde 500 em vez de 404.
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception e) {
        return respond("ENDPOINT_NOT_FOUND", "Rota nao encontrada", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        // Sem este log um erro inesperado vira 500 mudo, sem rastro nenhum.
        log.error("Erro nao tratado", e);
        return respond("INTERNAL_ERROR", "Erro interno do servidor", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> respond(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, status.value()));
    }
}
