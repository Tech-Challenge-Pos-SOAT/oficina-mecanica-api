package com.postech.oficinamecanica.domain.materialtransaction;

import java.time.Instant;

public class MaterialTransaction {
    private final Long id;
    private final Long materialId;
    private final Long serviceOrderId;
    private final Integer quantity;
    private final TransactionType type;
    private final Instant createdAt;

    public MaterialTransaction(
        Long id,
        Long materialId,
        Long serviceOrderId,
        Integer quantity,
        TransactionType type,
        Instant createdAt
    ) {
        if (materialId == null) throw new IllegalArgumentException("materialId");
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        if (type == null) throw new IllegalArgumentException("type");
        if (createdAt == null) throw new IllegalArgumentException("createdAt");

        this.id = id;
        this.materialId = materialId;
        this.serviceOrderId = serviceOrderId;
        this.quantity = quantity;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static MaterialTransaction in(Long materialId, Integer quantity, Instant createdAt) {
        return new MaterialTransaction(null, materialId, null, quantity, TransactionType.IN, createdAt);
    }

    /** Estorno: material volta ao estoque, mas mantendo o vinculo com a ordem que o consumiu. */
    public static MaterialTransaction returned(Long materialId, Long serviceOrderId, Integer quantity, Instant createdAt) {
        if (serviceOrderId == null) throw new IllegalArgumentException("serviceOrderId");
        return new MaterialTransaction(null, materialId, serviceOrderId, quantity, TransactionType.IN, createdAt);
    }

    public static MaterialTransaction out(Long materialId, Long serviceOrderId, Integer quantity, Instant createdAt) {
        if (serviceOrderId == null) throw new IllegalArgumentException("serviceOrderId");
        return new MaterialTransaction(null, materialId, serviceOrderId, quantity, TransactionType.OUT, createdAt);
    }

    public Long getId() { return id; }
    public Long getMaterialId() { return materialId; }
    public Long getServiceOrderId() { return serviceOrderId; }
    public Integer getQuantity() { return quantity; }
    public TransactionType getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
}
