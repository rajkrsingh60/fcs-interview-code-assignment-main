package com.fulfilment.application.monolith.warehouses.domain.rules;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class WarehouseValidationRules {

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

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
