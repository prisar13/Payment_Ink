# payment_service

Spring Boot service that handles authentication (JWT), transaction lifecycle, persistence, and communication with the fraud engine.

## Runs on

- `http://localhost:8081` (default)

## Key responsibilities

- **Auth**: register/login, issues JWTs, role-based access control
- **Transactions**: create transactions, list paginated results
- **Fraud integration**
  - Publishes `transaction.created` events to Kafka
  - Fetches fraud alerts from `Sentinal` (proxy endpoint)
  - Receives callbacks from `Sentinal` to update transaction status

## API (main)

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/assign-role` (ADMIN only)
- `POST /transaction/create`
- `POST /transaction/statusUpdate` (service callback)
- `GET /transaction?page=0&size=10`
- `GET /fraud/alerts?page=0&size=10`

## Configuration

Credentials are not committed. See root `.env.example`.

Used properties (see `src/main/resources/application.properties`):

- `PAYMENT_DB_URL`, `PAYMENT_DB_USERNAME`, `PAYMENT_DB_PASSWORD`
- `FRAUD_SERVICE_URL` (defaults to `http://localhost:8080`)
- `KAFKA_BOOTSTRAP_SERVERS` (defaults to `localhost:9092`)

## Notes & lessons learned

1. JPA pagination is built-in via `PageRequest` (page/size/sort) and `Page<T>` results.
2. “Fire-and-forget” async calls to other services can be done with `CompletableFuture.runAsync`, usually via `RestTemplate` (or a client like OpenFeign).
3. Service-to-service auth header pattern:
    ```
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(jwtUtil.getServiceToken());
    HttpEntity<FraudRequestDTO> entity = new HttpEntity<>(fraudRequest, headers);
    ```
4. `@Value("${fraud.service.url}")` is a quick way to bind config from `application.properties` into code.
