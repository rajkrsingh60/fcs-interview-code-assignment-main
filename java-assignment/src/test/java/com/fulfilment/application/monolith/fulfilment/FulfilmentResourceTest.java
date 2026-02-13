
package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.StoreRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class FulfilmentResourceTest {

    @Inject
    FulfilmentRepository fulfilmentRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    WarehouseRepository warehouseRepository;

    @Inject
    StoreRepository storeRepository;

    private Product product;
    private Store store;
    private DbWarehouse warehouse1;
    private DbWarehouse warehouse2;
    private DbWarehouse warehouse3;

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean up only fulfilment data before each test
        fulfilmentRepository.deleteAll();

        product = new Product("Test Product");
        productRepository.persist(product);

        store = new Store("Test Store");
        storeRepository.persist(store);

        warehouse1 = new DbWarehouse();
        warehouse1.businessUnitCode = "WH1";
        warehouseRepository.persist(warehouse1);

        warehouse2 = new DbWarehouse();
        warehouse2.businessUnitCode = "WH2";
        warehouseRepository.persist(warehouse2);

        warehouse3 = new DbWarehouse();
        warehouse3.businessUnitCode = "WH3";
        warehouseRepository.persist(warehouse3);
    }

    @AfterEach
    @Transactional
    public void cleanup() {
        fulfilmentRepository.deleteAll();
        productRepository.deleteById(product.id);
        storeRepository.deleteById(store.id);
        warehouseRepository.deleteById(warehouse1.id);
        warehouseRepository.deleteById(warehouse2.id);
        warehouseRepository.deleteById(warehouse3.id);
    }


    @Test
    public void testAddFulfilment_success() {
        FulfilmentRequest request = new FulfilmentRequest();
        request.productId = product.id;
        request.warehouseId = warehouse1.id;
        request.storeId = store.id;

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/fulfilment")
                .then()
                .statusCode(201);

        request.warehouseId = warehouse2.id;

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/fulfilment")
                .then()
                .statusCode(201);
    }

    @Test
    public void testAddFulfilment_failure_exceedsLimit() {
        FulfilmentRequest request = new FulfilmentRequest();
        request.productId = product.id;
        request.storeId = store.id;

        // Add first two fulfilments
        request.warehouseId = warehouse1.id;
        given().contentType("application/json").body(request).when().post("/fulfilment").then().statusCode(201);
        request.warehouseId = warehouse2.id;
        given().contentType("application/json").body(request).when().post("/fulfilment").then().statusCode(201);

        // Try to add a third
        request.warehouseId = warehouse3.id;
        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/fulfilment")
                .then()
                .statusCode(409)
                .body(is("A product can only be fulfilled by a maximum of 2 warehouses per store."));
    }

    @Test
    public void testAddFulfilment_notFound() {
        FulfilmentRequest request = new FulfilmentRequest();
        request.productId = 999L; // Non-existent
        request.warehouseId = warehouse1.id;
        request.storeId = store.id;

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/fulfilment")
                .then()
                .statusCode(404);
    }
}
