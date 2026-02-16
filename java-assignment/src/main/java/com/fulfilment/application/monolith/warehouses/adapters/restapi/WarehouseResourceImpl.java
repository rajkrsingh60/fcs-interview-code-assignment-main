package com.fulfilment.application.monolith.warehouses.adapters.restapi;

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

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

    private final WarehouseRepository warehouseRepository;
    private final CreateWarehouseOperation createWarehouse;
    private final ReplaceWarehouseOperation replaceWarehouse;
    private final ArchiveWarehouseOperation archiveWarehouse;
    private final WarehouseMapper warehouseMapper;

    private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class.getName());

    @Inject
    public WarehouseResourceImpl(WarehouseRepository warehouseRepository,
                                 CreateWarehouseOperation createWarehouse,
                                 ReplaceWarehouseOperation replaceWarehouse,
                                 ArchiveWarehouseOperation archiveWarehouse,
                                 WarehouseMapper warehouseMapper) {
        this.warehouseRepository = warehouseRepository;
        this.createWarehouse = createWarehouse;
        this.replaceWarehouse = replaceWarehouse;
        this.archiveWarehouse = archiveWarehouse;
        this.warehouseMapper = warehouseMapper;
    }

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.getAll().stream()
                .map(warehouseMapper::toWarehouseResponse)
                .toList();
    }

    @Override
    public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
        WarehouseDTO warehouseDTO = warehouseMapper.toModelWareHouse(data);
        createWarehouse.create(warehouseDTO);
        DbWarehouse createdWarehouse = warehouseRepository.find("businessUnitCode", data.getBusinessUnitCode())
                .firstResultOptional()
                .orElseThrow(() -> new WebApplicationException("Unable to find the created warehouse.", 500));

        LOGGER.info("Warehouse created with id: " + createdWarehouse.id);

        return warehouseMapper.toApiWareHouse(createdWarehouse);
    }

    @Override
    public Warehouse getAWarehouseUnitByID(String id) {
        LOGGER.info("id: " + id);
        DbWarehouse dbWarehouse = findWarehouseByIdOrThrow(id);
        return warehouseMapper.toApiWareHouse(dbWarehouse);
    }

    @Override
    public void archiveAWarehouseUnitByID(String id) {
        DbWarehouse byId = findWarehouseByIdOrThrow(id);
        archiveWarehouse.archive(byId.toWarehouse());
    }

    @Override
    public Warehouse replaceTheCurrentActiveWarehouse(
            String businessUnitCode, @NotNull Warehouse data) {
        if (businessUnitCode == null || businessUnitCode.isBlank()) {
            throw new WebApplicationException("Business unit code was not set.", 422);
        }

        WarehouseDTO domain = warehouseMapper.toModelWareHouse(data);
        domain.businessUnitCode = businessUnitCode; // path param is the authority
        replaceWarehouse.replace(domain);

        DbWarehouse newWarehouse = warehouseRepository.find("businessUnitCode", businessUnitCode)
                .firstResultOptional()
                .orElseThrow(() -> new WebApplicationException("Could not find warehouse after replacement.", 500));

        return warehouseMapper.toApiWareHouse(newWarehouse);
    }

    private DbWarehouse findWarehouseByIdOrThrow(String id) {
        long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new WebApplicationException("Invalid warehouse ID format: " + id, 400);
        }
        return warehouseRepository.findByIdOptional(longId)
                .orElseThrow(() -> new WebApplicationException("Warehouse with id " + id + " does not exist.", 404));
    }
}
