package com.fulfilment.application.monolith.warehouses.domain.rules;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

public class WarehouseValidationRules {

    private static final Logger LOG = Logger.getLogger(WarehouseValidationRules.class);

    public void validate(WarehouseDTO warehouse) {
        if (warehouse == null) {
            throw new WebApplicationException("Warehouse must be provided.", Response.Status.BAD_REQUEST);
        }
    }

    public void validateUpsert(WarehouseDTO warehouse) {
        if (isBlank(warehouse.businessUnitCode)) {
            throw new WebApplicationException("Business unit code must be provided.", Response.Status.BAD_REQUEST);
        }
        if (isBlank(warehouse.location)) {
            throw new WebApplicationException("Location must be provided.", Response.Status.BAD_REQUEST);
        }
        if (warehouse.capacity == null || warehouse.capacity <= 0) {
            throw new WebApplicationException("Capacity must be > 0.", 422);
        }
        if (warehouse.stock == null || warehouse.stock < 0) {
            throw new WebApplicationException("Stock must be >= 0.", 422);
        }
    }

    public void validateForArchiving(WarehouseDTO warehouse) {
        if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
            throw new WebApplicationException("Warehouse businessUnitCode must be provided.", Response.Status.BAD_REQUEST);
        }
        if (warehouse.archivedAt != null) {
            throw new WebApplicationException("Warehouse already archived.", Response.Status.CONFLICT);
        }
    }

    public void validateBusinessUnitCode(WarehouseDTO existing, String businessUnitCode) {
        if (existing != null && existing.archivedAt == null) {
            LOG.warnf("Business unit code already exists: %s", businessUnitCode);
            throw new WebApplicationException("Business unit code already exists: " + businessUnitCode, Response.Status.CONFLICT);
        }
    }

    public void validateLocation(Location loc, String location) {
        if (loc == null) {
            LOG.warnf("Invalid location specified: %s", location);
            throw new WebApplicationException("Invalid location: " + location, 422);
        }
    }

    public void validateCapacityAndStock(WarehouseDTO warehouse, Location loc) {
        if (warehouse.capacity > loc.maxCapacity) {
            LOG.warnf("Capacity %d exceeds max capacity %d for location %s", warehouse.capacity, loc.maxCapacity, warehouse.location);
            throw new WebApplicationException(
                    "Capacity exceeds max capacity for location. max=" + loc.maxCapacity + ", requested=" + warehouse.capacity, 422);
        }
        if (warehouse.stock > warehouse.capacity) {
            LOG.warnf("Stock %d exceeds capacity %d", warehouse.stock, warehouse.capacity);
            throw new WebApplicationException("Stock cannot exceed capacity.", 422);
        }
    }

    public void validateReplacement(WarehouseDTO newWarehouse, WarehouseDTO current) {
        // Capacity accommodation: new capacity must hold the existing stock
        if (newWarehouse.capacity < current.stock) {
            LOG.warnf("New capacity %d cannot accommodate existing stock %d", newWarehouse.capacity, current.stock);
            throw new WebApplicationException(
                    "New capacity cannot accommodate existing stock. existingStock=" + current.stock + ", newCapacity=" + newWarehouse.capacity, 422);
        }
    }

    public void validateMaxWarehouses(Location loc, long activeAtTarget, String location) {
        if (activeAtTarget >= loc.maxNumberOfWarehouses) {
            LOG.warnf("Maximum number of warehouses reached for location: %s", location);
            throw new WebApplicationException(
                    "Maximum number of warehouses reached for location: " + location, 422);
        }
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}