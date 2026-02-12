package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Objects;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationGateway;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
  }

  @Override
  public void replace(WarehouseDTO newWarehouse) {
    // ---- basic validations ----
    if (newWarehouse == null) {
      throw new IllegalArgumentException("Warehouse must be provided.");
    }
    if (isBlank(newWarehouse.businessUnitCode)) {
      throw new IllegalArgumentException("Business unit code must be provided.");
    }
    if (isBlank(newWarehouse.location)) {
      throw new IllegalArgumentException("Location must be provided.");
    }
    if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be > 0.");
    }
    if (newWarehouse.stock == null || newWarehouse.stock < 0) {
      throw new IllegalArgumentException("Stock must be >= 0.");
    }

    // ---- find current active warehouse for BU code ----
    WarehouseDTO current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null || current.archivedAt != null) {
      throw new IllegalStateException(
              "Active warehouse not found for businessUnitCode=" + newWarehouse.businessUnitCode);
    }

    // ---- location validation ----
    Location loc = locationGateway.resolveByIdentifier(newWarehouse.location);
    if (loc == null) {
      throw new IllegalStateException("Invalid location: " + newWarehouse.location);
    }

    // ---- capacity/stock rules for the new warehouse ----
    if (newWarehouse.capacity > loc.maxCapacity) {
      throw new IllegalStateException(
              "Capacity exceeds max capacity for location. max=" + loc.maxCapacity + ", requested=" + newWarehouse.capacity);
    }
    if (newWarehouse.stock > newWarehouse.capacity) {
      throw new IllegalStateException("Stock cannot exceed capacity.");
    }

    // ---- replacement-specific validations ----
    // Capacity accommodation: new capacity must hold the existing stock
    if (newWarehouse.capacity < current.stock) {
      throw new IllegalStateException(
              "New capacity cannot accommodate existing stock. existingStock=" + current.stock + ", newCapacity=" + newWarehouse.capacity);
    }

    // Stock matching: stock must match old warehouse stock
    if (!Objects.equals(newWarehouse.stock, current.stock)) {
      throw new IllegalStateException(
              "New warehouse stock must match existing stock. existingStock=" + current.stock + ", newStock=" + newWarehouse.stock);
    }

    // ---- max warehouses at the target location ----
    // Count active warehouses at target location, excluding the one being replaced (same BU code).
    long activeAtTarget =
            warehouseStore.getAll().stream()
                    .filter(w -> w.archivedAt == null)
                    .filter(w -> Objects.equals(w.location, newWarehouse.location))
                    .filter(w -> !Objects.equals(w.businessUnitCode, current.businessUnitCode))
                    .count();

    if (activeAtTarget >= loc.maxNumberOfWarehouses) {
      throw new IllegalStateException(
              "Maximum number of warehouses reached for location: " + newWarehouse.location);
    }

    // ---- perform replacement: archive old + create new ----
    LocalDateTime now = LocalDateTime.now();

    current.archivedAt = now;
    warehouseStore.update(current); // ensure repository persists archivedAt

    newWarehouse.createdAt = (newWarehouse.createdAt != null) ? newWarehouse.createdAt : now;
    newWarehouse.archivedAt = null;

    warehouseStore.create(newWarehouse);
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
