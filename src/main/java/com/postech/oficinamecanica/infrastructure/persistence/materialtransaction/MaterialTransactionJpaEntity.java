package com.postech.oficinamecanica.infrastructure.persistence.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "material_transaction")
public class MaterialTransactionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long materialId;

    private Long serviceOrderId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public MaterialTransactionJpaEntity() {}

    public MaterialTransactionJpaEntity(Long id, Long materialId, Long serviceOrderId, Integer quantity, TransactionType type, Instant createdAt) {
        this.id = id;
        this.materialId = materialId;
        this.serviceOrderId = serviceOrderId;
        this.quantity = quantity;
        this.type = type;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getMaterialId() { return materialId; }
    public Long getServiceOrderId() { return serviceOrderId; }
    public Integer getQuantity() { return quantity; }
    public TransactionType getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
}
