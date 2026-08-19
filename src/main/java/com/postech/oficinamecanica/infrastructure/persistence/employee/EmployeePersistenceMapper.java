package com.postech.oficinamecanica.infrastructure.persistence.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmployeePersistenceMapper {

    Employee toDomain(EmployeeJpaEntity entity);

    EmployeeJpaEntity toPersistence(Employee domain);
}
