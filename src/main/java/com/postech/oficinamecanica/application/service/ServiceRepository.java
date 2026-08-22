package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.Service;
import java.util.List;
import java.util.Optional;

public interface ServiceRepository {
    Service save(Service service);
    Optional<Service> findById(Long id);
    Optional<Service> findByName(String name);
    List<Service> findAll();
}
