package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;

/**
 * Aviso ao cliente das mudancas de status que exigem uma decisao ou marcam o
 * fim de uma etapa. Falha de envio e' responsabilidade do adapter: nunca deve
 * impedir a transicao de status ja persistida.
 */
public interface ServiceOrderNotifier {
    void notifyBudgetSubmitted(ServiceOrder order, Customer customer);
    void notifyBudgetApproved(ServiceOrder order, Customer customer);
    void notifyBudgetRejected(ServiceOrder order, Customer customer, String reason);
    void notifyFinished(ServiceOrder order, Customer customer);
}
