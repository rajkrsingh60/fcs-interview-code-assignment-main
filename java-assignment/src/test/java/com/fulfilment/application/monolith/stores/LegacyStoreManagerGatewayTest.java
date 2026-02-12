package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class LegacyStoreManagerGatewayTest {


    @Test
    public void testWithActualLegacyGateway() {

        String path = "store";
        Store store = new Store("LONDON_ACT");
        store.quantityProductsInStock = 100;

        long createdStoreId =
                given()
                        .contentType(ContentType.JSON)
                        .body(store)
                        .when()
                        .post(path)
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath().getLong("id");

        ExtractableResponse<Response> newlyCreatedStore = given()
                .contentType(ContentType.JSON)
                .when()
                .get(path + "/" + createdStoreId)
                .then()
                .statusCode(200)
                .extract();

        assertEquals(createdStoreId, newlyCreatedStore.jsonPath().getLong("id"));
        assertEquals("LONDON_ACT", newlyCreatedStore.jsonPath().getString("name"));
        assertEquals(100, newlyCreatedStore.jsonPath().getInt("quantityProductsInStock"));

        Store updatedStore = new Store("LONDON_UPD");
        updatedStore.id = createdStoreId;
        updatedStore.quantityProductsInStock = 10;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedStore)
                .when()
                .put(path + "/" + createdStoreId)
                .then()
                .statusCode(200)
                .body("name", is("LONDON_UPD"))
                .body("quantityProductsInStock", is(10));

    }

}