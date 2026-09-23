package com.postech.oficinamecanica.infrastructure.notification;

import com.postech.oficinamecanica.application.serviceorder.ServiceOrderNotifier;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Envia por e-mail (SMTP via Brevo) o aviso de mudanca de status da ordem.
 * O status ja foi persistido antes de chegar aqui - falha de envio so' e'
 * logada, nunca propaga e desfaz a transicao.
 */
@Component
public class SmtpServiceOrderNotifierAdapter implements ServiceOrderNotifier {
    private static final Logger log = LoggerFactory.getLogger(SmtpServiceOrderNotifierAdapter.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpServiceOrderNotifierAdapter(JavaMailSender mailSender, @Value("${mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void notifyBudgetSubmitted(ServiceOrder order, Customer customer) {
        send(order, customer, "Orcamento enviado, aguardando sua aprovacao.", null);
    }

    @Override
    public void notifyBudgetApproved(ServiceOrder order, Customer customer) {
        send(order, customer, "Orcamento aprovado, o reparo entrou em execucao.", null);
    }

    @Override
    public void notifyBudgetRejected(ServiceOrder order, Customer customer, String reason) {
        send(order, customer, "Orcamento recusado.", reason);
    }

    @Override
    public void notifyFinished(ServiceOrder order, Customer customer) {
        send(order, customer, "Servico finalizado, aguardando retirada.", null);
    }

    private void send(ServiceOrder order, Customer customer, String mensagem, String motivo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(customer.getEmail());
        message.setSubject("Ordem de servico " + order.getId() + ": " + order.getStatus());
        message.setText(body(order, mensagem, motivo));
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail de notificacao da ordem {} para {}", order.getId(),
                customer.getEmail(), e);
        }
    }

    private String body(ServiceOrder order, String mensagem, String motivo) {
        StringBuilder texto = new StringBuilder()
            .append("Ordem de servico: ").append(order.getId()).append('\n')
            .append("Status: ").append(order.getStatus()).append('\n')
            .append(mensagem);
        if (motivo != null) {
            texto.append('\n').append("Motivo: ").append(motivo);
        }
        return texto.toString();
    }
}
