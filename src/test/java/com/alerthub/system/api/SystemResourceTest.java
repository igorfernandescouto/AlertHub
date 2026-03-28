package com.alerthub.system.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SystemResourceTest {

    @Test
    void shouldExposeApplicationStatus() {
        given()
                .when().get("/api/v1/system/status")
                .then()
                .statusCode(200)
                .body("name", equalTo("alert-hub"))
                .body("version", equalTo("0.1.0"))
                .body("timestamp", notNullValue());
    }
}
