package com.alerthub.tenant.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class TenantResourceTest {

    @Test
    void shouldBootstrapAdminLoginAndManageTenantUsers() {
        var session = com.alerthub.support.ApiTestSupport.bootstrapTenantAndLogin();

        String createdUserId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", session.authorizationHeader())
                .body(Map.of(
                        "fullName", "Operator One",
                        "email", "operator+" + session.tenantSlug() + "@alerthub.dev",
                        "password", "StrongPass123!",
                        "role", "OPERATOR",
                        "status", "ACTIVE"
                ))
                .when().post("/api/v1/users")
                .then()
                .statusCode(200)
                .body("role", equalTo("OPERATOR"))
                .extract()
                .path("id");

        given()
                .header("Authorization", session.authorizationHeader())
                .when().get("/api/v1/users")
                .then()
                .statusCode(200)
                .body("$", hasSize(2));

        given()
                .header("Authorization", session.authorizationHeader())
                .when().get("/api/v1/tenants/{tenantId}", session.tenantId())
                .then()
                .statusCode(200)
                .body("slug", equalTo(session.tenantSlug()));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", session.authorizationHeader())
                .body(Map.of("status", "INACTIVE"))
                .when().patch("/api/v1/users/{userId}/status", createdUserId)
                .then()
                .statusCode(200)
                .body("status", equalTo("INACTIVE"));
    }
}
