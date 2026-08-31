package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.application.serviceorder.ActiveEmployeeFinder;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferVehicleOwnershipUseCaseTest {
    private static final String NEW_OWNER_DOCUMENT = "111.444.777-35";

    @Mock private VehicleRepository vehicleRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ActiveEmployeeFinder activeEmployeeFinder;
    @InjectMocks private TransferVehicleOwnershipUseCase useCase;

    private Vehicle vehicleOwnedBy(Long ownerId) {
        return new Vehicle(3L, ownerId, new Plate("XYZ-9876"), "Fiat", "Uno", 2019,
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private Customer newOwner(Long id, EntityStatus status) {
        return new Customer(id, new Document(NEW_OWNER_DOCUMENT), "Joao Silva", "(31) 99123-4567",
            "joao@email.com", status, Instant.now(), Instant.now());
    }

    private TransferVehicleOwnershipCommand command() {
        return new TransferVehicleOwnershipCommand(3L, NEW_OWNER_DOCUMENT, 1L);
    }

    @Test
    void shouldAttachTheVehicleToTheSecondOwner() {
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicleOwnedBy(7L)));
        when(customerRepository.findByDocument(new Document(NEW_OWNER_DOCUMENT)))
            .thenReturn(Optional.of(newOwner(12L, EntityStatus.ACTIVE)));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        Vehicle result = useCase.execute(command());

        assertThat(result.getCustomerId()).isEqualTo(12L);
        verify(activeEmployeeFinder).findActive(1L);
    }

    @Test
    void shouldRejectTransferToTheCurrentOwner() {
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicleOwnedBy(12L)));
        when(customerRepository.findByDocument(new Document(NEW_OWNER_DOCUMENT)))
            .thenReturn(Optional.of(newOwner(12L, EntityStatus.ACTIVE)));

        assertThatThrownBy(() -> useCase.execute(command()))
            .isInstanceOf(BusinessRuleViolationException.class);

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void shouldRejectInactiveNewOwner() {
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicleOwnedBy(7L)));
        when(customerRepository.findByDocument(new Document(NEW_OWNER_DOCUMENT)))
            .thenReturn(Optional.of(newOwner(12L, EntityStatus.INACTIVE)));

        assertThatThrownBy(() -> useCase.execute(command()))
            .isInstanceOf(CustomerNotActiveException.class);
    }

    @Test
    void shouldFailWhenTheNewOwnerIsNotRegistered() {
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicleOwnedBy(7L)));
        when(customerRepository.findByDocument(new Document(NEW_OWNER_DOCUMENT)))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command()))
            .isInstanceOf(CustomerNotFoundException.class);
    }
}
