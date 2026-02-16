package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.rules.WarehouseValidationRules;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOG = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationGateway;
  private final WarehouseValidationRules warehouseValidationRules;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
    this.warehouseValidationRules = new WarehouseValidationRules();
  }

  @Override
  public void create(WarehouseDTO warehouse) {
    warehouseValidationRules.validate(warehouse);
    warehouseValidationRules.validateUpsert(warehouse);

    // 1) Business Unit Code Verification: must not already exist (active)
    WarehouseDTO existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null && existing.archivedAt == null) {
      LOG.warnf("Business unit code already exists: %s", warehouse.businessUnitCode);
      throw new WebApplicationException("Business unit code already exists: " + warehouse.businessUnitCode, Response.Status.CONFLICT);
    }
    LOG.infof("Creating warehouse: %s", warehouse.businessUnitCode);

    // 2) Location Validation: must be a known valid location
    Location loc = locationGateway.resolveByIdentifier(warehouse.location);
    if (loc == null) {
      LOG.warnf("Invalid location: %s", warehouse.location);
      throw new WebApplicationException("Invalid location: " + warehouse.location, 422);
    }

    // 3) Warehouse Creation Feasibility: max warehouses at location
    long activeAtLocation =
            warehouseStore.getAll().stream()
                    .filter(w -> w.location.equals(warehouse.location))
                    .count();

    if (activeAtLocation >= loc.maxNumberOfWarehouses) {
      LOG.warnf("Maximum number of warehouses reached for location: %s", warehouse.location);
      throw new WebApplicationException("Maximum number of warehouses reached for location: " + warehouse.location, 422);
    }

    // 4) Capacity and Stock Validation
    if (warehouse.capacity > loc.maxCapacity) {
      LOG.warnf("Capacity %d exceeds max capacity %d for location %s", warehouse.capacity, loc.maxCapacity, warehouse.location);
      throw new WebApplicationException(
              "Capacity exceeds max capacity for location. max=" + loc.maxCapacity + ", requested=" + warehouse.capacity, 422);
    }

    // timestamps
    if (warehouse.createdAt == null) {
      warehouse.createdAt = LocalDateTime.now();
    }
    warehouse.archivedAt = null;

    warehouseStore.create(warehouse);
    LOG.infof("Warehouse created successfully: %s", warehouse.businessUnitCode);
  }

}
