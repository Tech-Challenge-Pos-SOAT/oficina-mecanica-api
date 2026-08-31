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
@Table(name = "service_order_service")
public class ServiceOrderServiceJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrderJpaEntity serviceOrder;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean approved;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public ServiceOrderServiceJpaEntity() {}

    public ServiceOrderServiceJpaEntity(Long id, Long serviceId, BigDecimal price, boolean approved,
                                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.serviceId = serviceId;
        this.price = price;
        this.approved = approved;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getServiceId() { return serviceId; }
    public BigDecimal getPrice() { return price; }
    public boolean isApproved() { return approved; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public ServiceOrderJpaEntity getServiceOrder() { return serviceOrder; }

    public void setServiceOrder(ServiceOrderJpaEntity serviceOrder) { this.serviceOrder = serviceOrder; }
}
