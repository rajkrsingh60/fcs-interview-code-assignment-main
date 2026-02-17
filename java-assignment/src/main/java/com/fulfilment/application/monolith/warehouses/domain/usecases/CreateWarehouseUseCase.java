package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.rules.WarehouseValidationRules;
import jakarta.enterprise.context.ApplicationScoped;
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
        warehouseValidationRules.validateBusinessUnitCode(existing, warehouse.businessUnitCode);
        LOG.infof("Creating warehouse: %s", warehouse.businessUnitCode);

        // 2) Location Validation: must be a known valid location
        Location loc = locationGateway.resolveByIdentifier(warehouse.location);
        warehouseValidationRules.validateLocation(loc, warehouse.location);

        // 3) Warehouse Creation Feasibility: max warehouses at location
        long activeAtLocation =
                warehouseStore.getAll().stream()
                        .filter(w -> w.location.equals(warehouse.location))
                        .count();

        warehouseValidationRules.validateMaxWarehouses(loc, activeAtLocation, warehouse.location);

        // 4) Capacity and Stock Validation
        warehouseValidationRules.validateCapacityAndStock(warehouse, loc);

        // timestamps
        if (warehouse.createdAt == null) {
            warehouse.createdAt = LocalDateTime.now();
        }
        warehouse.archivedAt = null;

        warehouseStore.create(warehouse);
        LOG.infof("Warehouse created successfully: %s", warehouse.businessUnitCode);
    }

}