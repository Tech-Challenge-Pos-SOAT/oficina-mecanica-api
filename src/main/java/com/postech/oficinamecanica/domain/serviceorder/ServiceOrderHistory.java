package com.postech.oficinamecanica.domain.serviceorder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registro imutavel de uma transicao de status, com o preco vigente naquele
 * momento - e o que permite reconstruir a evolucao do orcamento.
 */
public class ServiceOrderHistory {
    private final Long id;
    private final ServiceOrderStatus status;
    private final BigDecimal price;
    private final AuthorType authorType;
    private final Long authorId;
    private final String observation;
    private final Instant createdAt;

    public ServiceOrderHistory(Long id, ServiceOrderStatus status, BigDecimal price,
                               AuthorType authorType, Long authorId, String observation, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.price = price;
        this.authorType = authorType;
        this.authorId = authorId;
        this.observation = observation;
        this.createdAt = createdAt;
    }

    public static ServiceOrderHistory of(ServiceOrderStatus status, BigDecimal price,
                                         AuthorType authorType, Long authorId, String observation) {
        return new ServiceOrderHistory(null, status, price, authorType, authorId, observation, Instant.now());
    }

    public Long getId() { return id; }
    public ServiceOrderStatus getStatus() { return status; }
    public BigDecimal getPrice() { return price; }
    public AuthorType getAuthorType() { return authorType; }
    public Long getAuthorId() { return authorId; }
    public String getObservation() { return observation; }
    public Instant getCreatedAt() { return createdAt; }
}
