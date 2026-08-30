package com.postech.oficinamecanica.domain.serviceorder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderStatusTest {

    @Test
    void shouldAllowOnlyTheTransitionsDescribedInTheUbiquitousLanguage() {
        assertThat(ServiceOrderStatus.RECEIVED.canTransitionTo(ServiceOrderStatus.IN_DIAGNOSIS)).isTrue();
        assertThat(ServiceOrderStatus.IN_DIAGNOSIS.canTransitionTo(ServiceOrderStatus.AWAITING_APPROVAL)).isTrue();
        assertThat(ServiceOrderStatus.IN_DIAGNOSIS.canTransitionTo(ServiceOrderStatus.FINISHED)).isTrue();
        assertThat(ServiceOrderStatus.AWAITING_APPROVAL.canTransitionTo(ServiceOrderStatus.IN_EXECUTION)).isTrue();
        assertThat(ServiceOrderStatus.AWAITING_APPROVAL.canTransitionTo(ServiceOrderStatus.FINISHED)).isTrue();
        assertThat(ServiceOrderStatus.IN_EXECUTION.canTransitionTo(ServiceOrderStatus.AWAITING_APPROVAL)).isTrue();
        assertThat(ServiceOrderStatus.IN_EXECUTION.canTransitionTo(ServiceOrderStatus.FINISHED)).isTrue();
        assertThat(ServiceOrderStatus.FINISHED.canTransitionTo(ServiceOrderStatus.DELIVERED)).isTrue();
    }

    @Test
    void shouldRejectShortcutsBetweenStatuses() {
        assertThat(ServiceOrderStatus.RECEIVED.canTransitionTo(ServiceOrderStatus.IN_EXECUTION)).isFalse();
        assertThat(ServiceOrderStatus.RECEIVED.canTransitionTo(ServiceOrderStatus.DELIVERED)).isFalse();
        assertThat(ServiceOrderStatus.IN_DIAGNOSIS.canTransitionTo(ServiceOrderStatus.IN_EXECUTION)).isFalse();
        assertThat(ServiceOrderStatus.FINISHED.canTransitionTo(ServiceOrderStatus.IN_EXECUTION)).isFalse();
    }

    @Test
    void shouldNotAllowAnyTransitionAfterDelivered() {
        for (ServiceOrderStatus status : ServiceOrderStatus.values()) {
            assertThat(ServiceOrderStatus.DELIVERED.canTransitionTo(status)).isFalse();
        }
    }

    @Test
    void shouldParseStatusIgnoringCase() {
        assertThat(ServiceOrderStatus.fromValue("in_execution")).isEqualTo(ServiceOrderStatus.IN_EXECUTION);
    }

    @Test
    void shouldRejectUnknownStatusValue() {
        assertThatThrownBy(() -> ServiceOrderStatus.fromValue("CANCELADA"))
            .isInstanceOf(InvalidServiceOrderStatusException.class);
    }
}
