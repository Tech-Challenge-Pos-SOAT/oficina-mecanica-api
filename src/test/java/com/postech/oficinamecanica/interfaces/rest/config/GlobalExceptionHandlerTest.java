package com.postech.oficinamecanica.interfaces.rest.config;

import com.postech.oficinamecanica.domain.customer.CustomerAlreadyActiveException;
import com.postech.oficinamecanica.domain.customer.CustomerAlreadyInactiveException;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.customer.DuplicateDocumentException;
import com.postech.oficinamecanica.domain.customer.DuplicateEmailException;
import com.postech.oficinamecanica.domain.customer.InvalidDocumentException;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.DuplicatePlateException;
import com.postech.oficinamecanica.domain.vehicle.InvalidPlateException;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.VehicleAlreadyActiveException;
import com.postech.oficinamecanica.domain.vehicle.VehicleAlreadyInactiveException;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapInvalidDocumentTo400() {
        ResponseEntity<ErrorResponse> response = handler.handle(new InvalidDocumentException("123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_DOCUMENT");
    }

    @Test
    void shouldMapDuplicateDocumentTo409() {
        ResponseEntity<ErrorResponse> response = handler.handle(new DuplicateDocumentException(new Document("52998224725")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_DOCUMENT");
    }

    @Test
    void shouldMapDuplicateEmailTo409() {
        ResponseEntity<ErrorResponse> response = handler.handle(new DuplicateEmailException("maria@email.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_EMAIL");
    }

    @Test
    void shouldMapCustomerNotFoundTo404() {
        ResponseEntity<ErrorResponse> response = handler.handle(new CustomerNotFoundException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    void shouldMapCustomerAlreadyActiveTo409() {
        ResponseEntity<ErrorResponse> response = handler.handle(new CustomerAlreadyActiveException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("CUSTOMER_ALREADY_ACTIVE");
    }

    @Test
    void shouldMapCustomerAlreadyInactiveTo409() {
        ResponseEntity<ErrorResponse> response = handler.handle(new CustomerAlreadyInactiveException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("CUSTOMER_ALREADY_INACTIVE");
    }

    @Test
    void shouldMapInvalidPlateTo400() {
        ResponseEntity<ErrorResponse> response = handler.handle(new InvalidPlateException("123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_PLATE");
    }

    @Test
    void shouldMapDuplicatePlateTo409() {
        ResponseEntity<ErrorResponse> response = handler.handle(new DuplicatePlateException(new Plate("ABC-1234")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_PLATE");
    }

    @Test
    void shouldMapVehicleNotFoundTo404() {
        ResponseEntity<ErrorResponse> response = handler.handle(new VehicleNotFoundException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("VEHICLE_NOT_FOUND");
    }

    @Test
    void shouldMapVehicleAlreadyActiveTo409() {
        ResponseEntity<ErrorResponse> response = handler.handle(new VehicleAlreadyActiveException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("VEHICLE_ALREADY_ACTIVE");
    }

    @Test
    void shouldMapVehicleAlreadyInactiveTo409() {
        ResponseEntity<ErrorResponse> response = handler.handle(new VehicleAlreadyInactiveException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("VEHICLE_ALREADY_INACTIVE");
    }

    @Test
    void shouldMapCustomerNotActiveTo409() {
        ResponseEntity<ErrorResponse> response = handler.handle(new CustomerNotActiveException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("CUSTOMER_NOT_ACTIVE");
    }

    @Test
    void shouldMapValidationErrorTo400WithFirstFieldMessage() {
        FieldError fieldError = new FieldError("createCustomerRequest", "name", "must not be blank");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handle(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("name: must not be blank");
    }

    @Test
    void shouldMapInvalidStatusTo400() {
        // mensagem que o EntityStatus.valueOf realmente lanca
        IllegalArgumentException erro = new IllegalArgumentException(
            "No enum constant com.postech.oficinamecanica.domain.shared.EntityStatus.ATIVO");

        ResponseEntity<ErrorResponse> response =
            handler.handleIllegalArgument(erro, new MockHttpServletRequest("GET", "/api/services/1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_STATUS");
    }

    @Test
    void shouldKeepTheOriginalMessageWhenTheErrorIsNotAboutStatus() {
        ResponseEntity<ErrorResponse> response =
            handler.handleIllegalArgument(
                new IllegalArgumentException("Quantity to add must be greater than zero"),
                new MockHttpServletRequest("POST", "/api/materials/1/stock-entries"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().message()).isEqualTo("Quantity to add must be greater than zero");
    }

    @Test
    void shouldMapInvalidStatusTo400WhenTheEndpointIsAStatusChangeEvenWithoutMessage() {
        // PATCH /api/services/{id}/status lanca IllegalArgumentException sem mensagem
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
            new IllegalArgumentException(),
            new MockHttpServletRequest("PATCH", "/api/services/1/status"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_STATUS");
    }

    @Test
    void shouldMapInvalidStatusTo400WhenTheRequestFiltersByStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/materials");
        request.setParameter("status", "ARCHIVED");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
            new IllegalArgumentException("No enum constant ARCHIVED"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_STATUS");
    }

    @Test
    void shouldNotTreatAMissingParameterNameAsAStatusProblem() {
        // o bug original: GET /api/customers/document sem -parameters no compilador
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
            new IllegalArgumentException("Name for argument of type [java.lang.String] not specified"),
            new MockHttpServletRequest("GET", "/api/customers/document"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_ARGUMENT");
    }

    @Test
    void shouldMapMissingRequiredParameterTo400() {
        ResponseEntity<ErrorResponse> response = handler.handleMissingParameter(
            new MissingServletRequestParameterException("document", "String"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("MISSING_PARAMETER");
        assertThat(response.getBody().message()).contains("document");
    }

    @Test
    void shouldMapGenericExceptionTo500() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
    }
}
