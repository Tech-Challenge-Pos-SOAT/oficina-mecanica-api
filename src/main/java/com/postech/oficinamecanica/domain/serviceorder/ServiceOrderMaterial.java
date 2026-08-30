package com.postech.oficinamecanica.domain.serviceorder;

import com.postech.oficinamecanica.domain.shared.exceptions.InvalidParametersException;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Material incluido na ordem, com preco unitario congelado no momento da
 * inclusao. "stockDebited" marca se a baixa de estoque desse item ja aconteceu
 * - reparo adicional aprova a mesma ordem de novo e so os itens novos podem
 * sair do estoque.
 */
public class ServiceOrderMaterial {
    private final Long id;
    private final Long materialId;
    private final Integer quantity;
    private final BigDecimal price;
    private boolean stockDebited;
    private final Instant createdAt;
    private Instant updatedAt;

    public ServiceOrderMaterial(Long id, Long materialId, Integer quantity, BigDecimal price,
                                boolean stockDebited, Instant createdAt, Instant updatedAt) {
        if (materialId == null) {
            throw new InvalidParametersException("materialId", "Material id is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new InvalidParametersException("quantity", "Quantity must be greater than zero");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidParametersException("price", "Material price cannot be negative");
        }
        this.id = id;
        this.materialId = materialId;
        this.quantity = quantity;
        this.price = price;
        this.stockDebited = stockDebited;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ServiceOrderMaterial of(Long materialId, Integer quantity, BigDecimal price) {
        Instant now = Instant.now();
        return new ServiceOrderMaterial(null, materialId, quantity, price, false, now, now);
    }

    public BigDecimal total() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public void markStockDebited() {
        this.stockDebited = true;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getMaterialId() { return materialId; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public boolean isStockDebited() { return stockDebited; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
