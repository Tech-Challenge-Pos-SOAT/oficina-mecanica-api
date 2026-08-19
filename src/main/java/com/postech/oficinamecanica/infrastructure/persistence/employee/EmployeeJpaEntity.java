package com.postech.oficinamecanica.infrastructure.persistence.employee;

import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "employee")
public class EmployeeJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public EmployeeJpaEntity() {}

    public EmployeeJpaEntity(Long id, String name, String email, String password, EmployeeRole role,
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

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public EmployeeRole getRole() { return role; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(EmployeeRole role) { this.role = role; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
