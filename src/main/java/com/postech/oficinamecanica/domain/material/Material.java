package com.postech.oficinamecanica.domain.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.InvalidParametersException;

import java.math.BigDecimal;
import java.time.Instant;

public class Material {
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final Integer stockMinimum;
    private EntityStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Material(Long id, String name, String description, BigDecimal price,
                    Integer stockQuantity, Integer stockMinimum, EntityStatus status,
                    Instant createdAt, Instant updatedAt) {

        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidParametersException("price", "Price cannot be negative");
        }
        if (stockQuantity != null && stockQuantity < 0) {
            throw new InvalidParametersException("stockQuantity", "Stock quantity cannot be negative");
        }
        if (stockMinimum != null && stockMinimum < 0) {
            throw new InvalidParametersException("stockMinimum", "Stock minimum cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.stockMinimum = stockMinimum;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getStockMinimum() { return stockMinimum; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void changeStatus(EntityStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }
}
