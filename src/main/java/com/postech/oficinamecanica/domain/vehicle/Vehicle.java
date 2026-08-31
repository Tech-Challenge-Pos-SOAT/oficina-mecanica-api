package com.postech.oficinamecanica.domain.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.time.Instant;

public class Vehicle {
    private final Long id;
    private Long customerId;
    private Plate plate;
    private String brand;
    private String model;
    private int year;
    private EntityStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Vehicle(Long id, Long customerId, Plate plate, String brand, String model,
                   int year, EntityStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Vehicle create(Long customerId, Plate plate, String brand, String model, int year) {
        Instant now = Instant.now();
        return new Vehicle(null, customerId, plate, brand, model, year, EntityStatus.ACTIVE, now, now);
    }

    public void activate() {
        if (status == EntityStatus.ACTIVE) {
            throw new VehicleAlreadyActiveException(id);
        }
        this.status = EntityStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (status == EntityStatus.INACTIVE) {
            throw new VehicleAlreadyInactiveException(id);
        }
        this.status = EntityStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Troca de dono. A OS ja aberta continua apontando para o dono da epoca,
     * porque ela copia o customerId no momento da abertura - por isso a
     * transferencia aqui nao mexe em ordem nenhuma.
     */
    public void transferTo(Long newCustomerId) {
        if (newCustomerId == null) {
            throw new IllegalArgumentException("newCustomerId");
        }
        this.customerId = newCustomerId;
        this.updatedAt = Instant.now();
    }

    public void updateDetails(Plate plate, String brand, String model, int year) {
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Plate getPlate() { return plate; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
