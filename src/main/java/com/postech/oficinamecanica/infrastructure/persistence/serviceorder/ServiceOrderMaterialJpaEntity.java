package com.postech.oficinamecanica.infrastructure.persistence.serviceorder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "service_order_material")
public class ServiceOrderMaterialJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrderJpaEntity serviceOrder;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_debited", nullable = false)
    private boolean stockDebited;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public ServiceOrderMaterialJpaEntity() {}

    public ServiceOrderMaterialJpaEntity(Long id, Long materialId, Integer quantity, BigDecimal price,
                                         boolean stockDebited, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.materialId = materialId;
        this.quantity = quantity;
        this.price = price;
        this.stockDebited = stockDebited;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getMaterialId() { return materialId; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public boolean isStockDebited() { return stockDebited; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public ServiceOrderJpaEntity getServiceOrder() { return serviceOrder; }

    public void setServiceOrder(ServiceOrderJpaEntity serviceOrder) { this.serviceOrder = serviceOrder; }
}
