package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

    @Inject
    private WarehouseRepository warehouseRepository;

    @Inject
    private CreateWarehouseOperation createWarehouse;
    @Inject
    private ReplaceWarehouseOperation replaceWarehouse;
    @Inject
    private ArchiveWarehouseOperation archiveWarehouse;

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
    }

    @Override
    public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
        WarehouseDTO warehouseDTO = toModelWareHouse(data);
        createWarehouse.create(warehouseDTO);
        return toWarehouseResponse(warehouseDTO);
    }

    @Override
    public Warehouse getAWarehouseUnitByID(String businessUnitCode) {
        WarehouseDTO byBusinessUnitCode = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
        if (byBusinessUnitCode == null) {
            throw new WebApplicationException("Warehouse with id " + businessUnitCode + " does not exist.", 404);
        }
        return toWarehouseResponse(byBusinessUnitCode);
    }

    @Override
    public void archiveAWarehouseUnitByID(String businessUnitCode) {

        WarehouseDTO byBusinessUnitCode = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
        if (byBusinessUnitCode == null) {
            throw new WebApplicationException("Warehouse with id " + businessUnitCode + " does not exist.", 404);
        }

        archiveWarehouse.archive(byBusinessUnitCode);

    }

    @Override
    public Warehouse replaceTheCurrentActiveWarehouse(
            String businessUnitCode, @NotNull Warehouse data) {
        if (businessUnitCode == null || businessUnitCode.isBlank()) {
            throw new WebApplicationException("Business unit code was not set.", 422);
        }

        WarehouseDTO domain = toModelWareHouse(data);
        domain.businessUnitCode = businessUnitCode; // path param is the authority
        replaceWarehouse.replace(domain);
        return toWarehouseResponse(domain);

    }

    private Warehouse toWarehouseResponse(
            WarehouseDTO warehouse) {
        var response = new Warehouse();
        response.setBusinessUnitCode(warehouse.businessUnitCode);
        response.setLocation(warehouse.location);
        response.setCapacity(warehouse.capacity);
        response.setStock(warehouse.stock);

        return response;
    }

    private WarehouseDTO toModelWareHouse(Warehouse warehouse) {

        WarehouseDTO modelWarehouse = new WarehouseDTO();

        modelWarehouse.businessUnitCode = warehouse.getBusinessUnitCode();
        modelWarehouse.location = warehouse.getLocation();
        modelWarehouse.capacity = warehouse.getCapacity();
        modelWarehouse.stock = warehouse.getStock();

        return modelWarehouse;
    }

}
