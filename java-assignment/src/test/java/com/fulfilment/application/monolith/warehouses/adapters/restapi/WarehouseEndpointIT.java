package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusIntegrationTest
public class WarehouseEndpointIT {

    private final String path = "warehouse";

    @Test
    public void testSimpleListWarehouses() {

        // List all, should have all 3 products the database has initially:
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
    }

//    @Test
//    public void testSimpleCheckingArchivingWarehouses() {
//
//        // List all, should have all 3 products the database has initially:
//        given()
//                .when()
//                .get(path)
//                .then()
//                .statusCode(200)
//                .body(
//                        containsString("MWH.001"),
//                        containsString("MWH.012"),
//                        containsString("MWH.023"),
//                        containsString("ZWOLLE-001"),
//                        containsString("AMSTERDAM-001"),
//                        containsString("TILBURG-001"));
//
//        // Archive the ZWOLLE-001:
//        given().when().delete(path + "/1").then().statusCode(204);
//
//        // List all, ZWOLLE-001 should be missing now:
//        given()
//                .when()
//                .get(path)
//                .then()
//                .statusCode(200)
//                .body(
//                        not(containsString("ZWOLLE-001")),
//                        containsString("AMSTERDAM-001"),
//                        containsString("TILBURG-001"));
//    }

    @Test
    public void createNewWareHouse() {

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("LON.001");
        warehouse.setCapacity(10);
        warehouse.setLocation("AMSTERDAM-001");
        warehouse.setStock(300);

        given()
                .contentType(ContentType.JSON)
                .body(warehouse)
                .when()
                .post(path)
                .then()
                .statusCode(200)
                .body("businessUnitCode", is("LON.001"));

        ExtractableResponse<Response> newlyCreatedWareHouse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(path + "/" + "LON.001")
                .then()
                .statusCode(200)
                .extract();

        assertEquals("LON.001", newlyCreatedWareHouse.jsonPath().getString("businessUnitCode"));
        assertEquals(10, newlyCreatedWareHouse.jsonPath().getInt("capacity"));
        assertEquals(300, newlyCreatedWareHouse.jsonPath().getInt("stock"));

    }
}
