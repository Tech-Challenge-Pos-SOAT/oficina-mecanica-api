package com.postech.oficinamecanica.infrastructure.persistence.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.AuthorType;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "service_order_history")
public class ServiceOrderHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrderJpaEntity serviceOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false)
    private AuthorType authorType;

    @Column(name = "author_id")
    private Long authorId;

    @Column(length = 500)
    private String observation;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public ServiceOrderHistoryJpaEntity() {}

    public ServiceOrderHistoryJpaEntity(Long id, ServiceOrderStatus status, BigDecimal price, AuthorType authorType,
                                        Long authorId, String observation, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.price = price;
        this.authorType = authorType;
        this.authorId = authorId;
        this.observation = observation;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public ServiceOrderStatus getStatus() { return status; }
    public BigDecimal getPrice() { return price; }
    public AuthorType getAuthorType() { return authorType; }
    public Long getAuthorId() { return authorId; }
    public String getObservation() { return observation; }
    public Instant getCreatedAt() { return createdAt; }
    public ServiceOrderJpaEntity getServiceOrder() { return serviceOrder; }

    public void setServiceOrder(ServiceOrderJpaEntity serviceOrder) { this.serviceOrder = serviceOrder; }
}
