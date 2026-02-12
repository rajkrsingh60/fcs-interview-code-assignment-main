package com.fulfilment.application.monolith.stores;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@QuarkusTest
class StoreEndpointTest {

    @InjectMock
    private LegacyStoreManagerGateway legacyGateway;

    private final String path = "store";


    @Test
    public void givenThereAreRecordsInDatabaseWhenUserQueriesThemAllRecordsShouldBeReturned() {

        // List all, should have all 3 products the database has initially:
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

        // ensure create wasn't called
        verify(legacyGateway, never()).createStoreOnLegacySystem(any());
        // also ensure update wasn't called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenThereAreRecordsInDatabaseWhenUserDeletesOneOfThemThenThatRecordShouldNotBeReturned() {

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

        // ensure create wasn't called
        verify(legacyGateway, never()).createStoreOnLegacySystem(any());
        // also ensure update wasn't called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenTheStoreCreatedSuccessfullyThenLegacyGatewayCreateShouldBeCalled() {

        Store store = new Store("LONDON_UK");
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

        // get the newly created store
        ExtractableResponse<Response> getResponse = given()
                .when()
                .get(path + "/" + createdStoreId)
                .then()
                .statusCode(200)
                .extract();

        assertEquals(createdStoreId, getResponse.jsonPath().getLong("id"));
        assertEquals("LONDON_UK", getResponse.jsonPath().getString("name"));
        assertEquals(100, getResponse.jsonPath().getInt("quantityProductsInStock"));

        // Because AFTER_SUCCESS runs after commit, it might be invoked very shortly after response.
        // Mockito's timeout() gives it a small window.
        verify(legacyGateway, timeout(2000).times(1))
                .createStoreOnLegacySystem(argThat(s ->
                        s != null
                                && "LONDON_UK".equals(s.name)
                                && s.quantityProductsInStock == 100
                ));

        // also ensure update wasn't called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenTheStoreCreationFailsThenLegacyGatewayCreateShouldNotBeCalled() {

        Store store = new Store("LONDON_UK");
        store.id = 9999L;
        store.quantityProductsInStock = 100;

        given()
                .contentType(ContentType.JSON)
                .body(store)
                .when()
                .post(path)
                .then()
                .statusCode(422)
                .body("error", is("Id was invalidly set on request."));

        // ensure create wasn't called
        verify(legacyGateway, never()).createStoreOnLegacySystem(any());
        // also ensure update wasn't called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenTheRecordsInDatabaseWhenUserQueriesForNonExistentStoreThenErrorShouldBeReturned() {

        long invalidId = 9999L;

        given()
                .contentType(ContentType.JSON)
                .when()
                .get(path + "/" + invalidId)
                .then()
                .statusCode(404)
                .body("error", is("Store with id of " + invalidId + " does not exist."));

        // ensure create wasn't called
        verify(legacyGateway, never()).createStoreOnLegacySystem(any());
        // also ensure update wasn't called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenTheRecordExistsInDatabaseWhenUserRequestsUpdateThenRecordShouldGetSuccessfullyUpdated() {

        Store store = new Store("LONDON_SOC");
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
        assertEquals("LONDON_SOC", newlyCreatedStore.jsonPath().getString("name"));
        assertEquals(100, newlyCreatedStore.jsonPath().getInt("quantityProductsInStock"));

        Store updatedStore = new Store("LONDON_LIV");
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
                .body("name", is("LONDON_LIV"))
                .body("quantityProductsInStock", is(10));

        //As we created the record create should be fired once
        verify(legacyGateway, timeout(2000).times(1))
                .createStoreOnLegacySystem(argThat(s ->
                        s != null
                                && "LONDON_SOC".equals(s.name)
                                && s.quantityProductsInStock == 100
                ));

        //As we updated the record create should be fired once
        verify(legacyGateway, timeout(2000).times(1))
                .updateStoreOnLegacySystem(argThat(s ->
                        s != null
                                && "LONDON_LIV".equals(s.name)
                                && s.quantityProductsInStock == 10
                ));

    }

    @Test
    public void givenTheRecordExistsInDatabaseWhenUserRequestsUpdateWithStoreNameNullThenRequestShouldBeRejected() {

        Store store = new Store("LONDON_ROM");
        store.quantityProductsInStock = 10;

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
        assertEquals("LONDON_ROM", newlyCreatedStore.jsonPath().getString("name"));
        assertEquals(10, newlyCreatedStore.jsonPath().getInt("quantityProductsInStock"));

        Store updatedStore = new Store();
        updatedStore.id = createdStoreId;
        updatedStore.quantityProductsInStock = 100;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedStore)
                .when()
                .put(path + "/" + createdStoreId)
                .then()
                .statusCode(422)
                .body("error", is("Store Name was not set on request."));

        //As we created the record create should be fired once
        verify(legacyGateway, timeout(2000).times(1))
                .createStoreOnLegacySystem(argThat(s ->
                        s != null
                                && "LONDON_ROM".equals(s.name)
                                && s.quantityProductsInStock == 10
                ));

        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenTheRecordNotExistsInDatabaseWhenUserRequestsUpdateThenRequestShouldBeRejected() {

        Store updatedStore = new Store("LONDON_ROM");
        updatedStore.id = 999999L;
        updatedStore.quantityProductsInStock = 100;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedStore)
                .when()
                .put(path + "/" + updatedStore.id)
                .then()
                .statusCode(404)
                .body("error", is("Store with id of " + updatedStore.id + " does not exist."));


        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).createStoreOnLegacySystem(any());

        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenTheUserWantsToPatchARecordWhenUserSendRequestWithStoreNameAsNullThenRequestShouldBeRejected() {

        Store updatedStore = new Store();
        updatedStore.id = 999999L;
        updatedStore.quantityProductsInStock = 100;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedStore)
                .when()
                .patch(path + "/" + updatedStore.id)
                .then()
                .statusCode(422)
                .body("error", is("Store Name was not set on request."));


        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).createStoreOnLegacySystem(any());

        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenTheRecordNotExistsInDatabaseWhenUserRequestsPatchThenRequestShouldBeRejected() {

        Store updatedStore = new Store("LONDON_ROM");
        updatedStore.id = 999999L;
        updatedStore.quantityProductsInStock = 100;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedStore)
                .when()
                .patch(path + "/" + updatedStore.id)
                .then()
                .statusCode(404)
                .body("error", is("Store with id of " + updatedStore.id + " does not exist."));


        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).createStoreOnLegacySystem(any());

        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }

    @Test
    public void givenTheRecordNotInDatabaseWhenUserCreatesAndUpdateThenRecordShouldGetSuccessfullyUpdated() {

        Store store = new Store("LONDON_PAT");
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
        assertEquals("LONDON_PAT", newlyCreatedStore.jsonPath().getString("name"));
        assertEquals(100, newlyCreatedStore.jsonPath().getInt("quantityProductsInStock"));

        Store updatedStore = new Store("LONDON_NOR");
        updatedStore.id = createdStoreId;
        updatedStore.quantityProductsInStock = 10;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedStore)
                .when()
                .patch(path + "/" + createdStoreId)
                .then()
                .statusCode(200)
                .body("name", is("LONDON_NOR"))
                .body("quantityProductsInStock", is(10));

        //As we created the record create should be fired once
        verify(legacyGateway, timeout(2000).times(1))
                .createStoreOnLegacySystem(argThat(s ->
                        s != null
                                && "LONDON_PAT".equals(s.name)
                                && s.quantityProductsInStock == 100
                ));

        //As we updated the record create should be fired once
        verify(legacyGateway, timeout(2000).times(1))
                .updateStoreOnLegacySystem(argThat(s ->
                        s != null
                                && "LONDON_NOR".equals(s.name)
                                && s.quantityProductsInStock == 10
                ));

    }

    @Test
    public void givenTheRecordNotExistsInDatabaseWhenUserRequestsDeleteThenRequestShouldBeRejected() {

        long invalidId = 999999L;


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete(path + "/" + invalidId)
                .then()
                .statusCode(404)
                .body("error", is("Store with id of " + invalidId + " does not exist."));


        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).createStoreOnLegacySystem(any());

        //As we updated the record rolled back legacy gateway should not be called
        verify(legacyGateway, never()).updateStoreOnLegacySystem(any());

    }


}