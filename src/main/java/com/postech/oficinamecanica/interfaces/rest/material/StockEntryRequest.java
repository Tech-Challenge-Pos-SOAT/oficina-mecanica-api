package com.postech.oficinamecanica.interfaces.rest.material;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockEntryRequest(
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than zero")
    Integer quantity
) {
}
