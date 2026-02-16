package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.rules.WarehouseValidationRules;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.Objects;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOG = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationGateway;
  private final WarehouseValidationRules warehouseValidationRules;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
    this.warehouseValidationRules = new WarehouseValidationRules();
  }

  @Override
  public void replace(WarehouseDTO newWarehouse) {
    // ---- basic validations ----
    warehouseValidationRules.validate(newWarehouse);
    warehouseValidationRules.validateUpsert(newWarehouse);

    // ---- find current active warehouse for BU code ----
    WarehouseDTO current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null || current.archivedAt != null) {
      LOG.warnf("Active warehouse not found for businessUnitCode=%s", newWarehouse.businessUnitCode);
      throw new WebApplicationException(
              "Active warehouse not found for businessUnitCode=" + newWarehouse.businessUnitCode, Response.Status.NOT_FOUND);
    }

    LOG.infof("Replacing warehouse for business unit code: %s", newWarehouse.businessUnitCode);

    // ---- location validation ----
    Location loc = locationGateway.resolveByIdentifier(newWarehouse.location);
    if (loc == null) {
      LOG.warnf("Invalid location specified for replacement: %s", newWarehouse.location);
      throw new WebApplicationException("Invalid location: " + newWarehouse.location, 422);
    }

    // ---- capacity/stock rules for the new warehouse ----
    if (newWarehouse.capacity > loc.maxCapacity) {
      LOG.warnf("New capacity %d exceeds max capacity %d for location %s", newWarehouse.capacity, loc.maxCapacity, newWarehouse.location);
      throw new WebApplicationException(
              "Capacity exceeds max capacity for location. max=" + loc.maxCapacity + ", requested=" + newWarehouse.capacity, 422);
    }
    if (newWarehouse.stock > newWarehouse.capacity) {
      LOG.warnf("New stock %d exceeds new capacity %d", newWarehouse.stock, newWarehouse.capacity);
      throw new WebApplicationException("Stock cannot exceed capacity.", 422);
    }

    // ---- replacement-specific validations ----
    // Capacity accommodation: new capacity must hold the existing stock
    if (newWarehouse.capacity < current.stock) {
      LOG.warnf("New capacity %d cannot accommodate existing stock %d", newWarehouse.capacity, current.stock);
      throw new WebApplicationException(
              "New capacity cannot accommodate existing stock. existingStock=" + current.stock + ", newCapacity=" + newWarehouse.capacity, 422);
    }

    // ---- max warehouses at the target location ----
    // Count active warehouses at target location, excluding the one being replaced (same BU code).
    long activeAtTarget =
            warehouseStore.getAll().stream()
                    .filter(w -> Objects.equals(w.location, newWarehouse.location))
                    .filter(w -> !Objects.equals(w.businessUnitCode, current.businessUnitCode))
                    .count();

    if (activeAtTarget >= loc.maxNumberOfWarehouses) {
      LOG.warnf("Maximum number of warehouses reached for location: %s", newWarehouse.location);
      throw new WebApplicationException(
              "Maximum number of warehouses reached for location: " + newWarehouse.location, 422);
    }

    // ---- perform replacement: archive old + create new ----
    LOG.infof("Archiving old warehouse and creating new one for %s", newWarehouse.businessUnitCode);
    LocalDateTime now = LocalDateTime.now();

    current.archivedAt = now;
    warehouseStore.update(current); // ensure repository persists archivedAt

    newWarehouse.createdAt = (newWarehouse.createdAt != null) ? newWarehouse.createdAt : now;
    newWarehouse.archivedAt = null;

    warehouseStore.create(newWarehouse);
    LOG.infof("Successfully replaced warehouse for business unit code: %s", newWarehouse.businessUnitCode);
  }
}
