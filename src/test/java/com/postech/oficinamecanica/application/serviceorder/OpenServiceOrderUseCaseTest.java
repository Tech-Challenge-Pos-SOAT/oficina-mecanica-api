package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.application.service.ServiceRepository;
import com.postech.oficinamecanica.application.vehicle.VehicleRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.material.InactiveMaterialException;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.service.InactiveServiceException;
import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import com.postech.oficinamecanica.domain.serviceorder.VehicleNotOwnedByCustomerException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenServiceOrderUseCaseTest {
    private static final String DOCUMENT = "529.982.247-25";
    private static final String PLATE = "ABC-1234";

    @Mock private ServiceOrderRepository serviceOrderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ActiveEmployeeFinder activeEmployeeFinder;
    @Mock private ServiceRepository serviceRepository;
    @Mock private MaterialRepository materialRepository;
    @InjectMocks private OpenServiceOrderUseCase useCase;

    private Customer customer(EntityStatus status) {
        return new Customer(7L, new Document(DOCUMENT), "Maria Oliveira", "(31) 98765-4321",
            "maria@email.com", status, Instant.now(), Instant.now());
    }

    private Vehicle vehicle(Long ownerId) {
        return new Vehicle(3L, ownerId, new Plate(PLATE), "Fiat", "Uno", 2019,
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private Service service(EntityStatus status) {
        return new Service(5L, "Troca de oleo", "Motor flex", new BigDecimal("150.00"),
            status, Instant.now(), Instant.now());
    }

    private Material material(EntityStatus status) {
        return new Material(9L, "Filtro de Oleo", "Linha leve", new BigDecimal("32.50"),
            25, 5, status, Instant.now(), Instant.now());
    }

    private OpenServiceOrderCommand commandWithItems() {
        return new OpenServiceOrderCommand(DOCUMENT, PLATE, 1L, List.of(5L),
            List.of(new OpenServiceOrderCommand.MaterialItem(9L, 2)));
    }

    private void givenCustomerAndVehicle() {
        when(customerRepository.findByDocument(new Document(DOCUMENT))).thenReturn(Optional.of(customer(EntityStatus.ACTIVE)));
        when(vehicleRepository.findByPlate(new Plate(PLATE))).thenReturn(Optional.of(vehicle(7L)));
    }

    private OpenServiceOrderCommand command() {
        return new OpenServiceOrderCommand(DOCUMENT, PLATE, 1L);
    }

    @Test
    void shouldOpenOrderForActiveCustomerAndOwnedVehicle() {
        when(customerRepository.findByDocument(new Document(DOCUMENT))).thenReturn(Optional.of(customer(EntityStatus.ACTIVE)));
        when(vehicleRepository.findByPlate(new Plate(PLATE))).thenReturn(Optional.of(vehicle(7L)));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder order = useCase.execute(command());

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(order.getCustomerId()).isEqualTo(7L);
        assertThat(order.getVehicleId()).isEqualTo(3L);
        assertThat(order.getHistory()).hasSize(1);
        verify(activeEmployeeFinder).findActive(1L);
    }

    @Test
    void shouldRejectVehicleThatBelongsToAnotherCustomer() {
        when(customerRepository.findByDocument(new Document(DOCUMENT))).thenReturn(Optional.of(customer(EntityStatus.ACTIVE)));
        when(vehicleRepository.findByPlate(new Plate(PLATE))).thenReturn(Optional.of(vehicle(99L)));

        assertThatThrownBy(() -> useCase.execute(command()))
            .isInstanceOf(VehicleNotOwnedByCustomerException.class);

        verify(serviceOrderRepository, never()).save(any());
    }

    @Test
    void shouldRejectInactiveCustomer() {
        when(customerRepository.findByDocument(new Document(DOCUMENT))).thenReturn(Optional.of(customer(EntityStatus.INACTIVE)));

        assertThatThrownBy(() -> useCase.execute(command()))
            .isInstanceOf(CustomerNotActiveException.class);

        verify(serviceOrderRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenCustomerDocumentIsUnknown() {
        when(customerRepository.findByDocument(new Document(DOCUMENT))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command()))
            .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void shouldFailWhenPlateIsUnknown() {
        when(customerRepository.findByDocument(new Document(DOCUMENT))).thenReturn(Optional.of(customer(EntityStatus.ACTIVE)));
        when(vehicleRepository.findByPlate(new Plate(PLATE))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command()))
            .isInstanceOf(VehicleNotFoundException.class);
    }

    @Test
    void shouldOpenOrderAlreadyWithServicesAndMaterialsPricedFromCatalog() {
        givenCustomerAndVehicle();
        when(serviceRepository.findById(5L)).thenReturn(Optional.of(service(EntityStatus.ACTIVE)));
        when(materialRepository.findById(9L)).thenReturn(Optional.of(material(EntityStatus.ACTIVE)));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder order = useCase.execute(commandWithItems());

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(order.getServices()).hasSize(1);
        assertThat(order.getMaterials()).hasSize(1);
        assertThat(order.getPrice()).isEqualByComparingTo(new BigDecimal("215.00"));
        verify(materialRepository, never()).save(any());
    }

    @Test
    void shouldNotTouchCatalogsWhenNoItemsAreInformed() {
        givenCustomerAndVehicle();
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder order = useCase.execute(command());

        assertThat(order.getServices()).isEmpty();
        assertThat(order.getMaterials()).isEmpty();
        verify(serviceRepository, never()).findById(any());
        verify(materialRepository, never()).findById(any());
    }

    @Test
    void shouldRejectUnknownServiceAndNotSaveTheOrder() {
        givenCustomerAndVehicle();
        when(serviceRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(commandWithItems()))
            .isInstanceOf(ServiceNotFoundException.class);

        verify(serviceOrderRepository, never()).save(any());
    }

    @Test
    void shouldRejectInactiveServiceAndNotSaveTheOrder() {
        givenCustomerAndVehicle();
        when(serviceRepository.findById(5L)).thenReturn(Optional.of(service(EntityStatus.INACTIVE)));

        assertThatThrownBy(() -> useCase.execute(commandWithItems()))
            .isInstanceOf(InactiveServiceException.class);

        verify(serviceOrderRepository, never()).save(any());
    }

    @Test
    void shouldRejectInactiveMaterialAndNotSaveTheOrder() {
        givenCustomerAndVehicle();
        when(serviceRepository.findById(5L)).thenReturn(Optional.of(service(EntityStatus.ACTIVE)));
        when(materialRepository.findById(9L)).thenReturn(Optional.of(material(EntityStatus.INACTIVE)));

        assertThatThrownBy(() -> useCase.execute(commandWithItems()))
            .isInstanceOf(InactiveMaterialException.class);

        verify(serviceOrderRepository, never()).save(any());
    }
}
