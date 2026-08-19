package com.postech.oficinamecanica.infrastructure.persistence.vehicle;

import com.postech.oficinamecanica.application.vehicle.VehicleRepository;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class VehicleRepositoryImpl implements VehicleRepository {
    private final VehicleJpaRepository jpaRepository;
    private final VehiclePersistenceMapper mapper;

    public VehicleRepositoryImpl(VehicleJpaRepository jpaRepository, VehiclePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Vehicle> findByStatus(EntityStatus status) {
        return jpaRepository.findByStatusOrderById(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return mapper.toDomain(jpaRepository.save(mapper.toPersistence(vehicle)));
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByPlate(Plate plate) {
        return jpaRepository.findByPlate(plate.value()).map(mapper::toDomain);
    }
}
