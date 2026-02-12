package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class ReplaceWarehouseUseCaseTest {

    @Inject
    private ReplaceWarehouseOperation replaceWarehouseOperation;

    @Inject
    private WarehouseStore warehouseStore;

    @Test
    public void whenUserTriesToReplaceWarehouseItShouldFailWarehouseIsNull() {

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseOperation.replace(null)
        );

        assertEquals("Warehouse must be provided.", illegalArgumentException.getMessage());
    }

    @Test
    public void whenUserTriesToReplaceWarehouseItShouldFailIfBusinessUnitCodeIsNull() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.capacity = 10;
        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.stock = 10;

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("Business unit code must be provided.", illegalArgumentException.getMessage());

    }

    @Test
    public void whenUserTriesToReplaceeWarehouseItShouldFailIfLocationIsNull() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.capacity = 10;
        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.stock = 10;

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("Location must be provided.", illegalArgumentException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfCapacityIsNullOrZero() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.stock = 10;

        IllegalArgumentException illegalArgumentExceptionCapacityNull = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("Capacity must be > 0.", illegalArgumentExceptionCapacityNull.getMessage());

        warehouseDTO.capacity = 0;
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );
    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfStockIsNullOrZero() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.capacity = 10;

        IllegalArgumentException illegalArgumentExceptionStockNull = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("Stock must be >= 0.", illegalArgumentExceptionStockNull.getMessage());

        warehouseDTO.stock = -1;
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("Stock must be >= 0.", illegalArgumentException.getMessage());

    }

    @Test
    public void whenUserTriesToReplaceWarehouseItShouldFailIfTheLocationIsInvalid() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "MWH.001";
        warehouseDTO.capacity = 1;
        warehouseDTO.location = "LONDON-001";
        warehouseDTO.stock = 10;

        IllegalStateException illegalStateException = assertThrows(
                IllegalStateException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("Invalid location: " + warehouseDTO.location, illegalStateException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfCapacityIsMoreThanLocationCapacity() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "MWH.001";
        warehouseDTO.capacity = 1000;
        warehouseDTO.location = "VETSBY-001";
        warehouseDTO.stock = 10;

        IllegalStateException illegalStateException = assertThrows(
                IllegalStateException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("Capacity exceeds max capacity for location. max=90, requested=1000", illegalStateException.getMessage());

    }

    @Test
    public void whenUserTriesToReplaceWarehouseItShouldFailIfStockExceedsCapacity() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "MWH.001";
        warehouseDTO.capacity = 10;
        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.stock = 11;

        IllegalStateException illegalStateException = assertThrows(
                IllegalStateException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("Stock cannot exceed capacity.", illegalStateException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfNewCapacityLessThanCurrentStock() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "MWH.001";
        warehouseDTO.capacity = 9;
        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.stock = 9;

        IllegalStateException illegalStateException = assertThrows(
                IllegalStateException.class,
                () -> replaceWarehouseOperation.replace(warehouseDTO)
        );

        assertEquals("New capacity cannot accommodate existing stock. existingStock=10, newCapacity=9", illegalStateException.getMessage());

    }

    @Test
    public void whenUserTriesToReplaceWarehouseItShouldSucceedIfAllConditionPasses() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "MWH.001";
        warehouseDTO.capacity = 10;
        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.stock = 10;

        replaceWarehouseOperation.replace(warehouseDTO);

        WarehouseDTO replacedWarehouseDTO = warehouseStore.findByBusinessUnitCode("MWH.001");

        assertEquals("MWH.001", replacedWarehouseDTO.businessUnitCode);
        assertEquals(100, replacedWarehouseDTO.capacity);
        assertEquals("ZWOLLE-001", replacedWarehouseDTO.location);
        assertEquals(10, replacedWarehouseDTO.stock);

    }







}
