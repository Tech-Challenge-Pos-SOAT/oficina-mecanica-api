package com.postech.oficinamecanica.domain.serviceorder;

import com.postech.oficinamecanica.domain.shared.exceptions.InvalidParametersException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Raiz do agregado. Servicos, materiais e historico so mudam por metodo daqui:
 * nada de fora altera item ou status direto. O orcamento e recalculado a cada
 * inclusao, e toda transicao registra historico com o preco vigente.
 */
public class ServiceOrder {
    private final Long id;
    private final Long customerId;
    private final Long vehicleId;
    private BigDecimal price;
    private ServiceOrderStatus status;
    private final List<ServiceOrderService> services;
    private final List<ServiceOrderMaterial> materials;
    private final List<ServiceOrderHistory> history;
    private final Instant createdAt;
    private Instant updatedAt;

    public ServiceOrder(Long id, Long customerId, Long vehicleId, BigDecimal price, ServiceOrderStatus status,
                        List<ServiceOrderService> services, List<ServiceOrderMaterial> materials,
                        List<ServiceOrderHistory> history, Instant createdAt, Instant updatedAt) {
        if (customerId == null) {
            throw new InvalidParametersException("customerId", "Customer id is required");
        }
        if (vehicleId == null) {
            throw new InvalidParametersException("vehicleId", "Vehicle id is required");
        }
        if (status == null) {
            throw new InvalidParametersException("status", "Status is required");
        }
        this.id = id;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.price = price;
        this.status = status;
        this.services = services == null ? new ArrayList<>() : new ArrayList<>(services);
        this.materials = materials == null ? new ArrayList<>() : new ArrayList<>(materials);
        this.history = history == null ? new ArrayList<>() : new ArrayList<>(history);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Abertura: cliente vigente e veiculo ja resolvidos pelo caso de uso. */
    public static ServiceOrder open(Long customerId, Long vehicleId, Long employeeId) {
        Instant now = Instant.now();
        ServiceOrder order = new ServiceOrder(null, customerId, vehicleId, null, ServiceOrderStatus.RECEIVED,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), now, now);
        order.history.add(ServiceOrderHistory.of(ServiceOrderStatus.RECEIVED, null, AuthorType.EMPLOYEE, employeeId, null));
        return order;
    }

    public void startDiagnosis(Long employeeId) {
        transitionTo(ServiceOrderStatus.IN_DIAGNOSIS, AuthorType.EMPLOYEE, employeeId, null);
    }

    public void addService(Long serviceId, BigDecimal catalogPrice) {
        requireOpenForItems();
        services.add(ServiceOrderService.of(serviceId, catalogPrice));
        recalculateBudget();
    }

    public void addMaterial(Long materialId, Integer quantity, BigDecimal catalogPrice) {
        requireOpenForItems();
        materials.add(ServiceOrderMaterial.of(materialId, quantity, catalogPrice));
        recalculateBudget();
    }

    /** Envio do orcamento ao cliente. */
    public void submitForApproval(Long employeeId) {
        if (services.isEmpty() && materials.isEmpty()) {
            throw new EmptyServiceOrderException(id);
        }
        transitionTo(ServiceOrderStatus.AWAITING_APPROVAL, AuthorType.EMPLOYEE, employeeId, null);
    }

    public void approveBudget() {
        transitionTo(ServiceOrderStatus.IN_EXECUTION, AuthorType.CUSTOMER, customerId, null);
    }

    public void rejectBudget(String reason) {
        transitionTo(ServiceOrderStatus.FINISHED, AuthorType.CUSTOMER, customerId, reason);
    }

    public void finish(Long employeeId, String observation) {
        transitionTo(ServiceOrderStatus.FINISHED, AuthorType.EMPLOYEE, employeeId, observation);
    }

    public void deliver(Long employeeId) {
        transitionTo(ServiceOrderStatus.DELIVERED, AuthorType.EMPLOYEE, employeeId, null);
    }

    /** Materiais aprovados que ainda nao sairam do estoque. */
    public List<ServiceOrderMaterial> pendingStockDebit() {
        return materials.stream().filter(material -> !material.isStockDebited()).toList();
    }

    public void markStockDebited() {
        materials.forEach(ServiceOrderMaterial::markStockDebited);
        this.updatedAt = Instant.now();
    }

    private void recalculateBudget() {
        BigDecimal total = BigDecimal.ZERO;
        for (ServiceOrderService service : services) {
            total = total.add(service.total());
        }
        for (ServiceOrderMaterial material : materials) {
            total = total.add(material.total());
        }
        this.price = total;
        this.updatedAt = Instant.now();
    }

    /**
     * Itens entram na abertura (servico que o cliente ja pede no balcao), no
     * diagnostico e durante a execucao (reparo adicional). Depois que o
     * orcamento foi enviado, a ordem esta congelada ate o cliente decidir.
     */
    private void requireOpenForItems() {
        if (status != ServiceOrderStatus.RECEIVED
                && status != ServiceOrderStatus.IN_DIAGNOSIS
                && status != ServiceOrderStatus.IN_EXECUTION) {
            throw new ServiceOrderNotOpenForItemsException(id, status);
        }
    }

    private void transitionTo(ServiceOrderStatus target, AuthorType authorType, Long authorId, String observation) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidServiceOrderTransitionException(id, status, target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
        this.history.add(ServiceOrderHistory.of(target, price, authorType, authorId, observation));
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getVehicleId() { return vehicleId; }
    public BigDecimal getPrice() { return price; }
    public ServiceOrderStatus getStatus() { return status; }
    public List<ServiceOrderService> getServices() { return List.copyOf(services); }
    public List<ServiceOrderMaterial> getMaterials() { return List.copyOf(materials); }
    public List<ServiceOrderHistory> getHistory() { return List.copyOf(history); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
