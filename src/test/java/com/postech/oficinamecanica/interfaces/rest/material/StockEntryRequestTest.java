package com.postech.oficinamecanica.interfaces.rest.material;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StockEntryRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldAcceptValidStockEntryRequest() {
        StockEntryRequest request = new StockEntryRequest(10);
        Set<ConstraintViolation<StockEntryRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectNullQuantity() {
        StockEntryRequest request = new StockEntryRequest(null);
        Set<ConstraintViolation<StockEntryRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectZeroQuantity() {
        StockEntryRequest request = new StockEntryRequest(0);
        Set<ConstraintViolation<StockEntryRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectNegativeQuantity() {
        StockEntryRequest request = new StockEntryRequest(-5);
        Set<ConstraintViolation<StockEntryRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
