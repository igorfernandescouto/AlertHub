# AlertHub

AlertHub is a multi-tenant backend for event ingestion, alert rule evaluation, notification history, and audit trails.
The project is built with Java 25 and Quarkus and is structured as a production-style backend portfolio project.

## Delivered MVP

- tenant creation and tenant isolation
- bootstrap admin creation per tenant
- JWT authentication with role-based access
- tenant user management
- alert rule CRUD
- event ingestion with idempotency
- asynchronous event processing through messaging channels
- notification history
- audit trail history
- Swagger and OpenAPI documentation
- integration tests for the critical flow

## Stack

- Java 25
- Quarkus 3.32.3
- Quarkus REST + Jackson
- Hibernate ORM with Panache
- PostgreSQL
- Flyway
- Kafka
- SmallRye JWT
- JUnit 5
- Rest Assured
- Docker Compose

## Project structure

- `com.alerthub.auth`: authentication and token issuance
- `com.alerthub.tenant`: tenant lifecycle
- `com.alerthub.user`: bootstrap admin and tenant users
- `com.alerthub.rule`: alert rules and rule evaluation metadata
- `com.alerthub.event`: ingestion and asynchronous processing pipeline
- `com.alerthub.notification`: notification history
- `com.alerthub.audit`: audit trail
- `com.alerthub.shared`: shared API, security, paging, and domain primitives

The codebase uses package-by-feature so each slice keeps its API, application logic, domain model, and persistence close together.

## Local run

Start infrastructure:

```bash
docker compose up -d
```

Run the application:

```bash
mvn quarkus:dev
```

Run tests:

```bash
mvn test
```

Useful URLs:

- Swagger UI: `http://localhost:8080/docs`
- OpenAPI: `http://localhost:8080/openapi`
- Health: `http://localhost:8080/q/health`

## Runtime notes

- the default runtime profile uses PostgreSQL and Kafka from `docker-compose.yml`
- tests use H2 in memory and switch messaging channels to the in-memory connector
- JWT signing keys are stored under `src/main/resources/keys`

## Main flow

1. Create a tenant
2. Bootstrap the tenant admin
3. Login and get a JWT
4. Create users and alert rules
5. Ingest events
6. Consume notification and audit history

## API examples

Create a tenant:

```bash
curl -X POST http://localhost:8080/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Northwind",
    "slug": "northwind",
    "status": "ACTIVE"
  }'
```

Bootstrap the first admin:

```bash
curl -X POST http://localhost:8080/api/v1/tenants/<TENANT_ID>/users/bootstrap-admin \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Tenant Admin",
    "email": "admin@northwind.dev",
    "password": "StrongPass123!"
  }'
```

Login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantSlug": "northwind",
    "email": "admin@northwind.dev",
    "password": "StrongPass123!"
  }'
```

Create a rule:

```bash
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Order failure high severity",
    "eventType": "ORDER_FAILED",
    "payloadField": "severity",
    "operator": "EQUALS",
    "expectedValue": "HIGH",
    "priority": "CRITICAL",
    "channel": "INTERNAL",
    "messageTemplate": "Order failure with level {{value}} from {{source}}",
    "active": true
  }'
```

Ingest an event:

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "ORDER_FAILED",
    "source": "order-service",
    "payload": {
      "severity": "HIGH",
      "orderId": "ORD-123"
    },
    "occurredAt": "2026-01-01T10:00:00Z",
    "idempotencyKey": "evt-order-123"
  }'
```

Check notification history:

```bash
curl -X GET "http://localhost:8080/api/v1/notifications?page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>"
```

Check audit history:

```bash
curl -X GET "http://localhost:8080/api/v1/audits?page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>"
```

## Endpoint summary

- `POST /api/v1/tenants`
- `GET /api/v1/tenants`
- `GET /api/v1/tenants/{tenantId}`
- `PATCH /api/v1/tenants/{tenantId}/status`
- `POST /api/v1/tenants/{tenantId}/users/bootstrap-admin`
- `POST /api/v1/auth/login`
- `POST /api/v1/users`
- `GET /api/v1/users`
- `GET /api/v1/users/{userId}`
- `PATCH /api/v1/users/{userId}/status`
- `POST /api/v1/rules`
- `GET /api/v1/rules`
- `GET /api/v1/rules/{ruleId}`
- `PUT /api/v1/rules/{ruleId}`
- `DELETE /api/v1/rules/{ruleId}`
- `POST /api/v1/events`
- `POST /api/v1/events/{eventId}/reprocess`
- `GET /api/v1/events`
- `GET /api/v1/notifications`
- `GET /api/v1/audits`

## Next evolutions

- external delivery adapters for webhook and email
- dead letter queue
- scheduled or broker-driven retry policies
- metrics with Prometheus and Micrometer
- OIDC integration with an external identity provider
- admin dashboard
