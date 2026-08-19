package com.postech.oficinamecanica.domain.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.time.Instant;

public class Customer {
    private final Long id;
    private final Document document;
    private String name;
    private String phone;
    private String email;
    private EntityStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Customer(Long id, Document document, String name, String phone,
                    String email, EntityStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Customer create(Document document, String name, String phone, String email) {
        Instant now = Instant.now();
        return new Customer(null, document, name, phone, email, EntityStatus.ACTIVE, now, now);
    }

    public void activate() {
        if (status == EntityStatus.ACTIVE) {
            throw new CustomerAlreadyActiveException(id);
        }
        this.status = EntityStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (status == EntityStatus.INACTIVE) {
            throw new CustomerAlreadyInactiveException(id);
        }
        this.status = EntityStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void updateDetails(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
