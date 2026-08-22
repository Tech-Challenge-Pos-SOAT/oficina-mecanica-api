package com.postech.oficinamecanica.infrastructure.persistence.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vehicle")
public class VehicleJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false, unique = true)
    private String plate;

    @Column(nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public VehicleJpaEntity() {}

    public VehicleJpaEntity(Long id, Long customerId, String brand, String model, String plate,
                            int year, EntityStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.brand = brand;
        this.model = model;
        this.plate = plate;
        this.year = year;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getPlate() { return plate; }
    public int getYear() { return year; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setPlate(String plate) { this.plate = plate; }
    public void setYear(int year) { this.year = year; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
