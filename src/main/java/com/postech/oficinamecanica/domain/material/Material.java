package com.postech.oficinamecanica.domain.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.math.BigDecimal;
import java.time.Instant;

public class Material {
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final Integer stockMinimum;
    private final EntityStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Material(Long id, String name, String description, BigDecimal price,
                    Integer stockQuantity, Integer stockMinimum, EntityStatus status,
                    Instant createdAt, Instant updatedAt) {
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
}
