package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarehouseMapper {

    public Warehouse toWarehouseResponse(WarehouseDTO warehouse) {
        var response = new Warehouse();
        response.setBusinessUnitCode(warehouse.businessUnitCode);
        response.setLocation(warehouse.location);
        response.setCapacity(warehouse.capacity);
        response.setStock(warehouse.stock);
        return response;
    }

    public WarehouseDTO toModelWareHouse(Warehouse warehouse) {
        WarehouseDTO modelWarehouse = new WarehouseDTO();
        modelWarehouse.businessUnitCode = warehouse.getBusinessUnitCode();
        modelWarehouse.location = warehouse.getLocation();
        modelWarehouse.capacity = warehouse.getCapacity();
        modelWarehouse.stock = warehouse.getStock();
        return modelWarehouse;
    }

    public Warehouse toApiWareHouse(DbWarehouse warehouse) {
        Warehouse modelWarehouse = new Warehouse();
        modelWarehouse.setId(String.valueOf(warehouse.id));
        modelWarehouse.setBusinessUnitCode(warehouse.businessUnitCode);
        modelWarehouse.setLocation(warehouse.location);
        modelWarehouse.setCapacity(warehouse.capacity);
        modelWarehouse.setStock(warehouse.stock);
        return modelWarehouse;
    }
}
