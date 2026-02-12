package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationGateway;


  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
  }

  @Override
  public void create(WarehouseDTO warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must be provided.");
    }
    if (StringUtils.isBlank(warehouse.businessUnitCode)) {
      throw new IllegalArgumentException("Business unit code must be provided.");
    }
    if (StringUtils.isBlank(warehouse.location)) {
      throw new IllegalArgumentException("Location must be provided.");
    }
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be > 0.");
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock must be >= 0.");
    }

    // 1) Business Unit Code Verification: must not already exist (active)
    WarehouseDTO existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null && existing.archivedAt == null) {
      throw new IllegalStateException("Business unit code already exists: " + warehouse.businessUnitCode);
    }

    // 2) Location Validation: must be a known valid location
    Location loc = locationGateway.resolveByIdentifier(warehouse.location);
    if (loc == null) {
      throw new IllegalStateException("Invalid location: " + warehouse.location);
    }

    // 3) Warehouse Creation Feasibility: max warehouses at location
    long activeAtLocation =
            warehouseStore.getAll().stream()
                    .filter(w -> w.archivedAt == null)
                    .filter(w -> w.location.equals(warehouse.location))
                    .count();

    if (activeAtLocation >= loc.maxNumberOfWarehouses) {
      throw new IllegalStateException("Maximum number of warehouses reached for location: " + warehouse.location);
    }

    // 4) Capacity and Stock Validation
    if (warehouse.capacity > loc.maxCapacity) {
      throw new IllegalStateException(
              "Capacity exceeds max capacity for location. max=" + loc.maxCapacity + ", requested=" + warehouse.capacity);
    }

    // timestamps
    if (warehouse.createdAt == null) {
      warehouse.createdAt = LocalDateTime.now();
    }
    warehouse.archivedAt = null;

    warehouseStore.create(warehouse);
  }

}
