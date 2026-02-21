# ecom-payment-service

Payment processing microservice for the e-commerce platform. Handles payment processing, refunds, and payment status tracking.

## Tech Stack

- Java 21, Spring Boot 3.4.1, Spring Cloud 2024.0.0
- Spring Data JPA with H2 (in-memory)
- Eureka Client for service discovery
- SpringDoc OpenAPI for API documentation

## Port

**8085**

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/process` | Process a payment |
| GET | `/api/payments/{id}` | Get payment by ID |
| GET | `/api/payments/order/{orderId}` | Get payment by order ID |
| GET | `/api/payments/user/{userId}` | Get payments for a user |
| POST | `/api/payments/refund` | Process a refund |

## Notes

- Uses a mock payment processor with 90% success rate for development
- All payment transactions are recorded with unique transaction IDs

## Build and Run

```bash
mvn clean package
java -jar target/ecom-payment-service-0.0.1-SNAPSHOT.jar
```

## Access Points

- Swagger UI: http://localhost:8085/swagger-ui.html
- H2 Console: http://localhost:8085/h2-console
- Actuator Health: http://localhost:8085/actuator/health
