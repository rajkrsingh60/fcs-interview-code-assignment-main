package com.fulfilment.application.monolith.warehouses.domain.usecases;


import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class CreateWarehouseUseCaseTest {

    @Inject
    private CreateWarehouseOperation createWarehouseOperation;

    @Inject
    private WarehouseStore warehouseStore;


    @Test
    public void createWareHouse() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "IND.001";
        warehouseDTO.capacity = 10;
        warehouseDTO.location = "AMSTERDAM-001";
        warehouseDTO.stock = 10;

        createWarehouseOperation.create(warehouseDTO);

        WarehouseDTO existingWarehouseDTO = warehouseStore.findByBusinessUnitCode("IND.001");

        assertEquals("IND.001", existingWarehouseDTO.businessUnitCode);
        assertEquals(10, existingWarehouseDTO.capacity);
        assertEquals("AMSTERDAM-001", existingWarehouseDTO.location);
        assertEquals(10, existingWarehouseDTO.stock);


    }

    @Test
    public void giveTheBusinessUnitIsInDatabaseWhenUserTriesToCreateSameThenItShouldFail() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "IND.001";
        warehouseDTO.capacity = 10;
        warehouseDTO.location = "AMSTERDAM-001";
        warehouseDTO.stock = 10;

        WebApplicationException webApplicationException = assertThrows(WebApplicationException.class, () -> createWarehouseOperation.create(warehouseDTO));

        assertEquals("Business unit code already exists: " + warehouseDTO.businessUnitCode, webApplicationException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfMaximumCapacityHasReached() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.capacity = 10;
        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.stock = 10;

        WebApplicationException webApplicationException = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Maximum number of warehouses reached for location: " + warehouseDTO.location, webApplicationException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailWarehouseIsNull() {

        WebApplicationException webApplicationException = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(null)
        );

        assertEquals("Warehouse must be provided.", webApplicationException.getMessage());
    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfBusinessUnitCodeIsNull() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.capacity = 10;
        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.stock = 10;

        WebApplicationException webApplicationException = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Business unit code must be provided.", webApplicationException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfLocationIsNull() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.capacity = 10;
        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.stock = 10;

        WebApplicationException webApplicationException = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Location must be provided.", webApplicationException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfCapacityIsNullOrZero() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.stock = 10;

        WebApplicationException webApplicationExceptionNull = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Capacity must be > 0.", webApplicationExceptionNull.getMessage());

        warehouseDTO.capacity = 0;
        WebApplicationException webApplicationException = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Capacity must be > 0.", webApplicationException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfStockIsNullOrZero() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.location = "ZWOLLE-001";
        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.capacity = 10;

        WebApplicationException webApplicationExceptionNull = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Stock must be >= 0.", webApplicationExceptionNull.getMessage());

        warehouseDTO.stock = -1;
        WebApplicationException webApplicationException = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Stock must be >= 0.", webApplicationException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfCapacityIsMoreThanLocationCapacity() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.capacity = 1000;
        warehouseDTO.location = "VETSBY-001";
        warehouseDTO.stock = 10;

        WebApplicationException webApplicationException = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Capacity exceeds max capacity for location. max=90, requested=1000", webApplicationException.getMessage());

    }

    @Test
    public void whenUserTriesToCreateWarehouseItShouldFailIfTheLocationIsInvalid() {

        WarehouseDTO warehouseDTO = new WarehouseDTO();

        warehouseDTO.businessUnitCode = "IND.002";
        warehouseDTO.capacity = 1;
        warehouseDTO.location = "LONDON-001";
        warehouseDTO.stock = 10;

        WebApplicationException webApplicationException = assertThrows(
                WebApplicationException.class,
                () -> createWarehouseOperation.create(warehouseDTO)
        );

        assertEquals("Invalid location: " + warehouseDTO.location, webApplicationException.getMessage());

    }
}
