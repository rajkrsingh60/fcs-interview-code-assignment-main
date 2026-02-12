package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(WarehouseDTO warehouse) {

    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must be provided.");
    }

    // If your domain has id instead of businessUnitCode, validate id here too.
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse businessUnitCode must be provided.");
    }

    // Idempotent archive: calling archive twice shouldn't explode.
    if (warehouse.archivedAt != null) {
      throw new IllegalArgumentException("Warehouse already archived.");
    }

    warehouse.archivedAt = LocalDateTime.now();

    // Persist the state change (your repository should map archivedAt -> DbWarehouse.archivedAt)
    warehouseStore.remove(warehouse);

  }
}
