# FAKE PAYMENT PROVIDER

This service provides transactions (top up, withdrawal), sends webhooks, and checks the merchant balance.

## Technology Stack

- Java 21
- Spring WebFlux
- Spring Reactive Data
- Postgres
- Flyway
- TestContainers
- JUnit 5
- Mockito
- Docker

## Endpoints

The following endpoints are available for use:

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | /transactions | Create a new transaction (top up or withdrawal) |
| GET | /transactions/{transactionId} | Retrieve details of a specific transaction |
| GET | /transactions | Retrieve a list of transactions |

## OpenAPI Specification

You can find the OpenAPI specification (Swagger/OpenAPI 3.0) for this service in the `resources/api-docs.yaml` file.

To view the documentation, you can use tools like [Swagger UI](https://swagger.io/tools/swagger-ui/) or [Redoc](https://github.com/Redocly/redoc).

