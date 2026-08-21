package com.postech.oficinamecanica.domain.service;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.math.BigDecimal;
import java.time.Instant;

public class Service {
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final EntityStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Service(Long id, String name, String description, BigDecimal price,
                   EntityStatus status, Instant createdAt, Instant updatedAt) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidServicePriceException(price);
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Service update(String name, String description, BigDecimal price) {
        return new Service(id, name, description, price, status, createdAt, Instant.now());
    }

    public Service activate() {
        if (status == EntityStatus.ACTIVE) throw new ServiceAlreadyActiveException(id);
        return new Service(id, name, description, price, EntityStatus.ACTIVE, createdAt, Instant.now());
    }

    public Service deactivate() {
        if (status == EntityStatus.INACTIVE) throw new ServiceAlreadyInactiveException(id);
        return new Service(id, name, description, price, EntityStatus.INACTIVE, createdAt, Instant.now());
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
