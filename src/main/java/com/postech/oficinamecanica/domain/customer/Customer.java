package com.postech.oficinamecanica.domain.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.time.Instant;

public class Customer {
    private final Long id;
    private final Document document;
    private final String name;
    private final String phone;
    private final String email;
    private final EntityStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

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

    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
