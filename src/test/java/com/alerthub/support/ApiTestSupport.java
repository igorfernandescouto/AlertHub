package com.alerthub.support;

import static io.restassured.RestAssured.given;

import java.util.Map;
import java.util.UUID;

import io.restassured.http.ContentType;

public final class ApiTestSupport {

    private ApiTestSupport() {
    }

    public static TenantSession bootstrapTenantAndLogin() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String tenantSlug = "tenant-" + suffix;
        String email = "admin-" + suffix + "@alerthub.dev";
        String password = "StrongPass123!";

        String tenantId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Tenant " + suffix,
                        "slug", tenantSlug,
                        "status", "ACTIVE"
                ))
                .when().post("/api/v1/tenants")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "fullName", "Admin " + suffix,
                        "email", email,
                        "password", password
                ))
                .when().post("/api/v1/tenants/{tenantId}/users/bootstrap-admin", tenantId)
                .then()
                .statusCode(200);

        String token = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "tenantSlug", tenantSlug,
                        "email", email,
                        "password", password
                ))
                .when().post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");

        return new TenantSession(tenantId, tenantSlug, email, password, token);
    }

    public record TenantSession(
            String tenantId,
            String tenantSlug,
            String email,
            String password,
            String token
    ) {
        public String authorizationHeader() {
            return "Bearer " + token;
        }
    }
}
