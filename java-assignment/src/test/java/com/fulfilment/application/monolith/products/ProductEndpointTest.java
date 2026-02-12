package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ProductEndpointTest {

    private final String path = "product";

    @Test
    public void testCrudProduct() {

        // List all, should have all 3 products the database has initially:
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

        // Delete the TONSTAD:
        given().when().delete(path + "/1").then().statusCode(204);

        // List all, TONSTAD should be missing now:
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
    }


    @Test
    public void testCreateReadUpdateDeleteProduct() {

        Product product = new Product("BILLY");
        product.price = BigDecimal.ONE;
        product.description = "New chair for study";
        product.stock = 10;


        long createdProductId =
                given()
                        .contentType(ContentType.JSON)
                        .body(product)
                        .when()
                        .post(path)
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath().getLong("id");

        ExtractableResponse<Response> getResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(path + "/" + createdProductId)
                .then()
                .statusCode(200)
                .extract();

        assertEquals(createdProductId, getResponse.jsonPath().getLong("id"));
        assertEquals("BILLY", getResponse.jsonPath().getString("name"));
        assertEquals("New chair for study", getResponse.jsonPath().getString("description"));

    }

    @Test
    void create_shouldReturn422_whenIdIsProvided() {
        Product p = new Product();
        p.id = 999L;                 // <-- force invalid request
        p.name = "BILLY";
        p.description = "Bookcase";
        p.price = new BigDecimal("49.99");
        p.stock = 10;

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(p)
                .when()
                .post("/product")
                .then()
                .statusCode(422)
                .body("code", is(422))
                .body("error", is("Id was invalidly set on request."));
    }

    @Test
    void delete_shouldReturn404_whenIdNotFound() {
        long missingId = 999999L;

        given()
                .when()
                .delete("/product/" + missingId)
                .then()
                .statusCode(404)
                .body("code", is(404))
                .body("error", is("Product with id of " + missingId + " does not exist."));
    }

    @Test
    void getSingle_shouldReturn404_whenIdNotFound() {
        long missingId = 999999L;

        given()
                .when()
                .get("/product/" + missingId)
                .then()
                .statusCode(404)
                .body("code", is(404))
                .body("error", is("Product with id of " + missingId + " does not exist."));
    }

    @Test
    void update_shouldSucceed_whenProductExists() {
        // 1) Create
        Product createProduct = new Product("TEST_CREATE");
        createProduct.price = new BigDecimal("10.00");
        createProduct.description = "Creating product";
        createProduct.stock = 10;

        long createdProductId =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(createProduct)
                        .when()
                        .post("/product")
                        .then()
                        .statusCode(is(201))
                        .body("id", notNullValue())
                        .extract()
                        .jsonPath()
                        .getLong("id");

        // 2) Update (no id in body!)
        Product updateProduct = new Product("TEST_UPDATE");
        updateProduct.price = new BigDecimal("20.00");
        updateProduct.description = "Updating product";
        updateProduct.stock = 20;
        updateProduct.id = createdProductId;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updateProduct)
                .when()
                .put(path + "/" + createdProductId)
                .then()
                .statusCode(200)
                .body("name", is("TEST_UPDATE"))
                .body("description", is("Updating product"))
                .body("price", is(20.0f))
                .body("stock", is(20));

        // 3) Verify via GET
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/product/" + createdProductId)
                .then()
                .statusCode(200)
                .body("name", is("TEST_UPDATE"))
                .body("description", is("Updating product"))
                .body("price", is(20.0f))
                .body("stock", is(20));
    }

    @Test
    void update_shouldReturn422_whenNameIsNull() {
        // 1) Create
        Product createProduct = new Product("BILLY_V2");
        createProduct.price = new BigDecimal("10.00");
        createProduct.description = "Creating product";
        createProduct.stock = 10;

        long createdProductId =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(createProduct)
                        .when()
                        .post("/product")
                        .then()
                        .statusCode(is(201))
                        .body("id", notNullValue())
                        .extract()
                        .jsonPath()
                        .getLong("id");

        // 2) Update (no id in body!)
        Product updateProduct = new Product();
        updateProduct.price = new BigDecimal("20.00");
        updateProduct.description = "Updating product";
        updateProduct.stock = 20;
        updateProduct.id = createdProductId;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updateProduct)
                .when()
                .put("/product/" + createdProductId)
                .then()
                .statusCode(422)
                .body("error", is("Product Name was not set on request."));

    }

    @Test
    void update_shouldReturn404_whenProductIsNotFound() {
        // 1) Create
        // 2) Update (no id in body!)
        Product updateProduct = new Product("NON_EXISTENT_PRODUCT");
        updateProduct.price = new BigDecimal("20.00");
        updateProduct.description = "Update Non existent product";
        updateProduct.stock = 20;
        updateProduct.id = 9999L;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updateProduct)
                .when()
                .put("/product/" + updateProduct.id)
                .then()
                .statusCode(404)
                .body("error", is("Product with id of " + updateProduct.id + " does not exist."));

    }
}
