package com.postech.oficinamecanica.infrastructure.notification;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpServiceOrderNotifierAdapterTest {
    private static final String FROM = "oficina@oficina.com";

    @Mock private JavaMailSender mailSender;

    private SmtpServiceOrderNotifierAdapter notifier;

    @BeforeEach
    void setUp() {
        notifier = new SmtpServiceOrderNotifierAdapter(mailSender, FROM);
    }

    private Customer customer() {
        return new Customer(7L, new Document("529.982.247-25"), "Maria Oliveira", "(31) 98765-4321",
            "maria@email.com", EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private ServiceOrder order() {
        ServiceOrder order = ServiceOrder.open(7L, 3L, 1L);
        order.startDiagnosis(1L);
        order.addService(5L, new BigDecimal("120.00"));
        order.submitForApproval(1L);
        return order;
    }

    @Test
    void shouldSendPlainTextEmailToCustomerWhenBudgetIsSubmitted() {
        ServiceOrder order = order();

        notifier.notifyBudgetSubmitted(order, customer());

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo(FROM);
        assertThat(message.getTo()).containsExactly("maria@email.com");
        assertThat(message.getText()).contains("Ordem de servico: " + order.getId())
            .contains("Status: " + order.getStatus());
    }

    @Test
    void shouldIncludeReasonInBodyWhenBudgetIsRejected() {
        notifier.notifyBudgetRejected(order(), customer(), "Preco alto");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("Motivo: Preco alto");
    }

    @Test
    void shouldNotPropagateExceptionWhenMailSenderFails() {
        doThrow(new MailSendException("smtp indisponivel")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> notifier.notifyFinished(order(), customer())).doesNotThrowAnyException();
    }
}
