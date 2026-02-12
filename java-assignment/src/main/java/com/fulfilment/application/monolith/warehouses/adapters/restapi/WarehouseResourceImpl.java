package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.stores.StoreResource;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
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
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

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

    private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
    }

    @Override
    public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
        WarehouseDTO warehouseDTO = toModelWareHouse(data);
        createWarehouse.create(warehouseDTO);
        Optional<DbWarehouse> dbWarehouse = warehouseRepository.findAll()
                .stream()
                .filter(db ->
                        db.businessUnitCode
                                .equals(data.getBusinessUnitCode()))
                .findFirst();

        if (dbWarehouse.isEmpty()) {
            throw new WebApplicationException("Unable to create warehouse.", 404);
        } else {
            data.setId(String.valueOf(dbWarehouse.get().id));
        }

        LOGGER.info("Warehouse created with id: " + dbWarehouse.get().id);

        return data;
    }

    @Override
    public Warehouse getAWarehouseUnitByID(String id) {
        LOGGER.info("id: " + id);
        long longId = Long.parseLong(id);
        DbWarehouse byId = warehouseRepository.findById(longId);
        if (byId == null) {
            throw new WebApplicationException("Warehouse with id " + id + " does not exist.", 404);
        }
        return toApiWareHouse(byId);
    }

    @Override
    public void archiveAWarehouseUnitByID(String id) {

        long longId = Long.parseLong(id);
        DbWarehouse byId = warehouseRepository.findById(longId);
        if (byId == null) {
            throw new WebApplicationException("Warehouse with id " + id + " does not exist.", 404);
        }

        archiveWarehouse.archive(byId.toWarehouse());

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

    private Warehouse toApiWareHouse(DbWarehouse warehouse) {

        Warehouse modelWarehouse = new Warehouse();

        modelWarehouse.setId(String.valueOf(warehouse.id));
        modelWarehouse.setBusinessUnitCode(warehouse.businessUnitCode);
        modelWarehouse.setLocation(warehouse.location);
        modelWarehouse.setCapacity(warehouse.capacity);
        modelWarehouse.setStock(warehouse.stock);

        return modelWarehouse;
    }
}
