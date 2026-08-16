package com.postech.oficinamecanica.domain.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    @Test
    void shouldCreateCustomerWithActiveStatus() {
        Customer customer = Customer.create(
            new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com"
        );

        assertThat(customer.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(customer.getName()).isEqualTo("Maria Souza");
        assertThat(customer.getId()).isNull();
    }

    @Test
    void shouldDeactivateActiveCustomer() {
        Customer customer = Customer.create(
            new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com"
        );

        customer.deactivate();

        assertThat(customer.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldNotAllowDeactivatingTwice() {
        Customer customer = Customer.create(
            new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com"
        );
        customer.deactivate();

        assertThatThrownBy(customer::deactivate)
            .isInstanceOf(CustomerAlreadyInactiveException.class);
    }

    @Test
    void shouldActivateInactiveCustomer() {
        Customer customer = Customer.create(
            new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com"
        );
        customer.deactivate();

        customer.activate();

        assertThat(customer.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldNotAllowReactivatingCustomerThatIsAlreadyActive() {
        Customer customer = Customer.create(
            new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com"
        );

        assertThatThrownBy(customer::activate)
            .isInstanceOf(CustomerAlreadyActiveException.class);
    }

    @Test
    void shouldUpdateContactDetails() {
        Customer customer = Customer.create(
            new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com"
        );

        customer.updateDetails("Maria Souza Lima", "11999998888", "maria.lima@email.com");

        assertThat(customer.getName()).isEqualTo("Maria Souza Lima");
        assertThat(customer.getPhone()).isEqualTo("11999998888");
        assertThat(customer.getEmail()).isEqualTo("maria.lima@email.com");
    }
}
