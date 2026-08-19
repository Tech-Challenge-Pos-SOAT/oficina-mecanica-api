package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.DuplicatePlateException;
import com.postech.oficinamecanica.domain.vehicle.InvalidPlateException;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateVehicleUseCaseTest {
    @Mock
    private VehicleRepository repository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CreateVehicleUseCase useCase;

    @Test
    void shouldCreateVehicleWhenCustomerActiveAndPlateUnique() {
        CreateVehicleCommand cmd = new CreateVehicleCommand(1L, "Toyota", "Corolla", "ABC-1234", 2021);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer()));
        when(repository.findByPlate(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = useCase.execute(cmd);

        assertThat(result.getBrand()).isEqualTo("Toyota");
        verify(repository).save(any());
    }

    @Test
    void shouldFailWhenCustomerNotFound() {
        CreateVehicleCommand cmd = new CreateVehicleCommand(99L, "Toyota", "Corolla", "ABC-1234", 2021);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(CustomerNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenCustomerIsInactive() {
        CreateVehicleCommand cmd = new CreateVehicleCommand(1L, "Toyota", "Corolla", "ABC-1234", 2021);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(inactiveCustomer()));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(CustomerNotActiveException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenPlateAlreadyExists() {
        CreateVehicleCommand cmd = new CreateVehicleCommand(1L, "Toyota", "Corolla", "ABC-1234", 2021);
        Vehicle existing = Vehicle.create(2L, new com.postech.oficinamecanica.domain.vehicle.Plate("ABC-1234"), "Honda", "Civic", 2020);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer()));
        when(repository.findByPlate(any())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicatePlateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenSamePlateSentWithDifferentFormatting() {
        CreateVehicleCommand cmd = new CreateVehicleCommand(1L, "Toyota", "Corolla", "ABC1234", 2021);
        Vehicle existing = Vehicle.create(2L, new com.postech.oficinamecanica.domain.vehicle.Plate("ABC-1234"), "Honda", "Civic", 2020);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer()));
        when(repository.findByPlate(new com.postech.oficinamecanica.domain.vehicle.Plate("ABC1234")))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicatePlateException.class);
    }

    @Test
    void shouldFailWhenPlateFormatIsInvalid() {
        CreateVehicleCommand cmd = new CreateVehicleCommand(1L, "Toyota", "Corolla", "123", 2021);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer()));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(InvalidPlateException.class);
        verify(repository, never()).save(any());
    }

    private static Customer activeCustomer() {
        return new Customer(1L, new Document("52998224725"), "Maria Souza", "11987654321",
            "maria@email.com", EntityStatus.ACTIVE, java.time.Instant.now(), java.time.Instant.now());
    }

    private static Customer inactiveCustomer() {
        return new Customer(1L, new Document("52998224725"), "Maria Souza", "11987654321",
            "maria@email.com", EntityStatus.INACTIVE, java.time.Instant.now(), java.time.Instant.now());
    }
}
