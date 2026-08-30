package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.employee.EmployeeRepository;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
import org.springframework.stereotype.Service;

/**
 * Toda acao de funcionario na ordem precisa de um funcionario existente e
 * ativo - o autor vai para o historico. Concentrado aqui para nao repetir a
 * mesma checagem em cada caso de uso.
 */
@Service
public class ActiveEmployeeFinder {
    private final EmployeeRepository employeeRepository;

    public ActiveEmployeeFinder(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee findActive(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        if (employee.getStatus() != EntityStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Employee " + employeeId + " is not active");
        }
        return employee;
    }
}
