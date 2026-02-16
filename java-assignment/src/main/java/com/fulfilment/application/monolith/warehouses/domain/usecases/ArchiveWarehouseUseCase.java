package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.rules.WarehouseValidationRules;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOG = Logger.getLogger(ArchiveWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final WarehouseValidationRules warehouseValidationRules;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidationRules = new WarehouseValidationRules();
  }

  @Override
  public void archive(WarehouseDTO warehouse) {
    warehouseValidationRules.validate(warehouse);
    warehouseValidationRules.validateForArchiving(warehouse);

    // Idempotent archive: calling archive twice shouldn't explode.
    if (warehouse.archivedAt != null) {
      LOG.warnf("Warehouse already archived: %s", warehouse.businessUnitCode);
      throw new WebApplicationException("Warehouse already archived.", 409);
    }
    LOG.infof("Archiving warehouse: %s", warehouse.businessUnitCode);

    warehouse.archivedAt = LocalDateTime.now();

    // Persist the state change (your repository should map archivedAt -> DbWarehouse.archivedAt)
    warehouseStore.remove(warehouse);
    LOG.infof("Warehouse archived successfully: %s", warehouse.businessUnitCode);
  }
}
