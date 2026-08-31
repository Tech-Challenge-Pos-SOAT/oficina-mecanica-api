package com.postech.oficinamecanica.domain.serviceorder;

import com.postech.oficinamecanica.domain.shared.exceptions.InvalidParametersException;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Servico incluido na ordem. O preco e uma copia do catalogo no momento da
 * inclusao: mudanca posterior no catalogo nao altera ordem existente.
 * "approved" marca o servico que o cliente ja autorizou - e' o espelho do
 * stockDebited do material, e separa o que veio no primeiro orcamento do que
 * entrou em um reparo adicional ainda pendente.
 */
public class ServiceOrderService {
    private final Long id;
    private final Long serviceId;
    private final BigDecimal price;
    private boolean approved;
    private final Instant createdAt;
    private Instant updatedAt;

    public ServiceOrderService(Long id, Long serviceId, BigDecimal price, boolean approved,
                               Instant createdAt, Instant updatedAt) {
        if (serviceId == null) {
            throw new InvalidParametersException("serviceId", "Service id is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidParametersException("price", "Service price must be greater than zero");
        }
        this.id = id;
        this.serviceId = serviceId;
        this.price = price;
        this.approved = approved;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ServiceOrderService of(Long serviceId, BigDecimal price) {
        Instant now = Instant.now();
        return new ServiceOrderService(null, serviceId, price, false, now, now);
    }

    public BigDecimal total() {
        return price;
    }

    public void markApproved() {
        this.approved = true;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getServiceId() { return serviceId; }
    public BigDecimal getPrice() { return price; }
    public boolean isApproved() { return approved; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
