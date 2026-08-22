package com.postech.oficinamecanica.domain.employee;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.time.Instant;

public class Employee {
    private final Long id;
    private String name;
    private String email;
    private final String password;
    private EmployeeRole role;
    private EntityStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Employee(Long id, String name, String email, String password, EmployeeRole role,
                     EntityStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Employee create(String name, String email, String passwordHash, EmployeeRole role) {
        return new Employee(null, name, email, passwordHash, role, EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    public void updateProfile(String name, String email, EmployeeRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (status == EntityStatus.ACTIVE) {
            throw new EmployeeAlreadyActiveException(id);
        }
        this.status = EntityStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (status == EntityStatus.INACTIVE) {
            throw new EmployeeAlreadyInactiveException(id);
        }
        this.status = EntityStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public EmployeeRole getRole() { return role; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
