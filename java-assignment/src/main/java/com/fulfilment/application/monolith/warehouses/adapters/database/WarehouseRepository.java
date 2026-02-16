package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

    private static final Logger LOG = Logger.getLogger(WarehouseRepository.class);

    @Override
    public List<WarehouseDTO> getAll() {
        LOG.info("Fetching all active warehouses");
        return this.list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
    }

    @Override
    @Transactional
    public void create(WarehouseDTO warehouse) {
        LOG.infof("Creating warehouse entity for business unit code: %s", warehouse.businessUnitCode);
        DbWarehouse entity = new DbWarehouse();
        entity.businessUnitCode = warehouse.businessUnitCode;
        entity.location = warehouse.location;
        entity.capacity = warehouse.capacity;
        entity.stock = warehouse.stock;

        entity.createdAt = (warehouse.createdAt != null) ? warehouse.createdAt : LocalDateTime.now();
        entity.archivedAt = null;

        persist(entity);
        LOG.infof("Successfully persisted new warehouse for business unit code: %s", warehouse.businessUnitCode);
    }

    @Override
    public void update(WarehouseDTO warehouse) {
        LOG.infof("Updating warehouse entity for business unit code: %s", warehouse.businessUnitCode);
        DbWarehouse entity =
                find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();

        if (entity == null) {
            LOG.warnf("No active warehouse found to update for business unit code: %s", warehouse.businessUnitCode);
            throw new IllegalStateException(
                    "Active warehouse not found for businessUnitCode=" + warehouse.businessUnitCode);
        }

        entity.location = warehouse.location;
        entity.capacity = warehouse.capacity;
        entity.stock = warehouse.stock;
        entity.archivedAt = (warehouse.archivedAt != null) ? warehouse.archivedAt : LocalDateTime.now();
        LOG.infof("Successfully updated warehouse for business unit code: %s", warehouse.businessUnitCode);
    }

    @Override
    public void remove(WarehouseDTO warehouse) {
        LOG.infof("Archiving warehouse entity for business unit code: %s", warehouse.businessUnitCode);
        DbWarehouse entity =
                find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();

        if (entity == null) {
            LOG.warnf("No active warehouse found to archive for business unit code: %s. No-op.", warehouse.businessUnitCode);
            // choose behavior: no-op or throw. I'd no-op for delete semantics.
            return;
        }

        entity.archivedAt = LocalDateTime.now();
        LOG.infof("Successfully archived warehouse for business unit code: %s", warehouse.businessUnitCode);
    }

    @Override
    public WarehouseDTO findByBusinessUnitCode(String buCode) {
        LOG.infof("Fetching active warehouse for business unit code: %s", buCode);
        DbWarehouse entity =
                find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
        return entity == null ? null : entity.toWarehouse();
    }
}
