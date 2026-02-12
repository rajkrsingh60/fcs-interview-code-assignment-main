package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ArchiveWarehouseUseCaseTest {

    @Inject
    private ArchiveWarehouseOperation archiveWarehouseOperation;

    @Inject
    private CreateWarehouseOperation createWarehouseOperation;

    @Inject
    private WarehouseStore warehouseStore;

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailWarehouseIsNull() {

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> archiveWarehouseOperation.archive(null)
        );

        assertEquals("Warehouse must be provided.", illegalArgumentException.getMessage());
    }

    @Test
    public void whenUserTriesToArchiveWarehouseItShouldFailIfBusinessUnitCodeIsNull() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.capacity = 10;
        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.stock = 10;

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> archiveWarehouseOperation.archive(warehouseDTO)
        );

        assertEquals("Warehouse businessUnitCode must be provided.", illegalArgumentException.getMessage());

    }

    @Test
    public void whenUserTriesToArchiveWarehouseItShouldFailIfArchiveAtIsNotNull() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "IND.010";
        warehouseDTO.capacity = 10;
        warehouseDTO.location = "AMSTERDAM-001";
        warehouseDTO.stock = 10;
        warehouseDTO.archivedAt = LocalDateTime.now();

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> archiveWarehouseOperation.archive(warehouseDTO)
        );

        assertEquals("Warehouse already archived.", illegalArgumentException.getMessage());

    }

    @Test
    public void whenUserTriesToArchiveExistingWarehouseItShouldBeSuccessful() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "IND.011";
        warehouseDTO.capacity = 1;
        warehouseDTO.location = "AMSTERDAM-001";
        warehouseDTO.stock = 1;

        createWarehouseOperation.create(warehouseDTO);

        WarehouseDTO existingWarehouseDTO = warehouseStore.findByBusinessUnitCode("IND.011");
        assertNull(existingWarehouseDTO.archivedAt, "The warehouse is created, so archiveAt should be null");

        archiveWarehouseOperation.archive(existingWarehouseDTO);

        WarehouseDTO archivedWarehouseDTO = warehouseStore.findByBusinessUnitCode("IND.011");
        assertNotNull(archivedWarehouseDTO.archivedAt, "The warehouse is archived, so archiveAt should not be null");

    }

}
