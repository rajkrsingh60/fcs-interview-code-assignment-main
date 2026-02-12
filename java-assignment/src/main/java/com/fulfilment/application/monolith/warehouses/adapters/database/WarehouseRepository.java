package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

    @Override
    public List<WarehouseDTO> getAll() {
        return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
    }

    @Override
    @Transactional
    public void create(WarehouseDTO warehouse) {
        DbWarehouse entity = new DbWarehouse();
        entity.businessUnitCode = warehouse.businessUnitCode;
        entity.location = warehouse.location;
        entity.capacity = warehouse.capacity;
        entity.stock = warehouse.stock;

        entity.createdAt = (warehouse.createdAt != null) ? warehouse.createdAt : LocalDateTime.now();
        entity.archivedAt = null;

        persist(entity);
    }

    @Override
    public void update(WarehouseDTO warehouse) {
        DbWarehouse entity =
                find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();

        if (entity == null) {
            throw new IllegalStateException(
                    "Active warehouse not found for businessUnitCode=" + warehouse.businessUnitCode);
        }

        entity.location = warehouse.location;
        entity.capacity = warehouse.capacity;
        entity.stock = warehouse.stock;
        entity.archivedAt = (warehouse.archivedAt != null) ? warehouse.archivedAt : LocalDateTime.now();
    }

    @Override
    public void remove(WarehouseDTO warehouse) {
        DbWarehouse entity =
                find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();

        if (entity == null) {
            // choose behavior: no-op or throw. I'd no-op for delete semantics.
            return;
        }

        entity.archivedAt = LocalDateTime.now();
    }

    @Override
    public WarehouseDTO findByBusinessUnitCode(String buCode) {
        DbWarehouse entity =
                find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
        return entity == null ? null : entity.toWarehouse();
    }
}
