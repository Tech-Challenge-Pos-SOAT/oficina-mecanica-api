package com.postech.oficinamecanica.infrastructure.persistence.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_order")
public class ServiceOrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderStatus status;

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ServiceOrderServiceJpaEntity> services = new ArrayList<>();

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ServiceOrderMaterialJpaEntity> materials = new ArrayList<>();

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ServiceOrderHistoryJpaEntity> history = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public ServiceOrderJpaEntity() {}

    public ServiceOrderJpaEntity(Long id, Long customerId, Long vehicleId, BigDecimal price,
                                 ServiceOrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addService(ServiceOrderServiceJpaEntity service) {
        service.setServiceOrder(this);
        this.services.add(service);
    }

    public void addMaterial(ServiceOrderMaterialJpaEntity material) {
        material.setServiceOrder(this);
        this.materials.add(material);
    }

    public void addHistory(ServiceOrderHistoryJpaEntity entry) {
        entry.setServiceOrder(this);
        this.history.add(entry);
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getVehicleId() { return vehicleId; }
    public BigDecimal getPrice() { return price; }
    public ServiceOrderStatus getStatus() { return status; }
    public List<ServiceOrderServiceJpaEntity> getServices() { return services; }
    public List<ServiceOrderMaterialJpaEntity> getMaterials() { return materials; }
    public List<ServiceOrderHistoryJpaEntity> getHistory() { return history; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStatus(ServiceOrderStatus status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
