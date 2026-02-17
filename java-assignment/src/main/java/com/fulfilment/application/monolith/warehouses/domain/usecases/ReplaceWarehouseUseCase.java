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
    warehouseValidationRules.validateLocation(loc, newWarehouse.location);

    // ---- capacity/stock rules for the new warehouse ----
    warehouseValidationRules.validateCapacityAndStock(newWarehouse, loc);

    // ---- replacement-specific validations ----
    warehouseValidationRules.validateReplacement(newWarehouse, current);

    // ---- max warehouses at the target location ----
    // Count active warehouses at target location, excluding the one being replaced (same BU code).
    long activeAtTarget =
            warehouseStore.getAll().stream()
                    .filter(w -> Objects.equals(w.location, newWarehouse.location))
                    .filter(w -> !Objects.equals(w.businessUnitCode, current.businessUnitCode))
                    .count();

    warehouseValidationRules.validateMaxWarehouses(loc, activeAtTarget, newWarehouse.location);

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
