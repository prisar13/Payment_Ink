# Sentinal (fraud engine)

Spring Boot service that evaluates transactions for fraud. It consumes Kafka events from `payment_service`, persists evaluations, and sends status callbacks back to `payment_service` with retry/backoff if the callback fails.

## Runs on

- `http://localhost:8080` (default)

## Key responsibilities

- **Fraud evaluation API**: `/evaluate` for direct evaluation calls
- **Async evaluation**: consumes `transaction.created` from Kafka
- **Alerts**: `/alerts` paginated fraud alerts
- **Callback reliability**: retries `/transaction/statusUpdate` using an in-memory retry queue with exponential backoff

## API (main)

- `POST /evaluate`
- `GET /alerts?page=0&size=10`

## Configuration

Credentials are not committed. See root `.env.example`.

Used properties (see `src/main/resources/application.properties`):

- `FRAUD_DB_URL`, `FRAUD_DB_USERNAME`, `FRAUD_DB_PASSWORD`
- `SENTINAL_ADMIN_USER`, `SENTINAL_ADMIN_PASSWORD`
- `PAYMENT_SERVICE_URL` (defaults to `http://localhost:8081`)
- `REDIS_HOST`, `REDIS_PORT`
- `KAFKA_BOOTSTRAP_SERVERS`

## Notes & lessons learned

- **Async + distributed design**
  1. Async processing improves perceived latency for the caller.
  2. Clear separation of concerns matters in distributed systems.
- **Retry/backoff**
  3. Retry logic for status update includes a retry limit to avoid resource exhaustion.
  4. TODO: In-memory queue can be replaced with Kafka, a `PriorityQueue` ordered by next retry time, or Java’s `DelayQueue` (often the cleanest).
- **Service boundaries**
  6. `payment_service` owns the transaction ID (UUID). Fraud treats it as an external reference—no ownership in fraud service.
  7. Idempotency matters: if status is already “in review” or new status == old status, don’t update.
  8. Retry callbacks cover the case where `payment_service` is temporarily down.
- **HTTP client gotchas**
  9. Treat non-2xx as failure; depending on error handlers, `RestTemplate` may not throw how you expect.
- **Spring Security / DI**
  10. Adding `spring-boot-starter-security` auto-registers a `SecurityFilterChain`; without deliberate config, you can get unexpected 401s.
  11. Spring injects primarily by type.
  12. Two beans of the same type (e.g., `PasswordEncoder`) requires `@Qualifier` to resolve ambiguity.
  13. Singleton beans are created at application context initialization.
  14. BCrypt is intentionally slow and salted; good for passwords. Fast hashes (e.g., SHA256) are not appropriate for password storage.
  15. Never trust request data—trust DB state.
  16. Services are business logic; don’t mix them into the security lifecycle.
  17. Spring roles are typically prefixed with `ROLE_` (e.g., `ADMIN` → `ROLE_ADMIN`).
  18. Method-level security is evaluated after authentication, before method execution.
  19. Authentication needs roles immediately → eager fetch can be required.
  20. Stateless auth: every request validates JWT and sets `SecurityContext`.
- **DB migrations & performance**
  21. Flyway migration naming: `V<version>__<description>.sql`
  22. Indexes (e.g., evaluatedAt + decision) can materially speed up searches.
  23. Fraud rules to revisit: IP address, country, userId.
- **Kafka semantics**
  24. If Kafka auto-config doesn’t load: missing dependency, version mismatch, excluded auto-config, or overridden factory beans; ensure `@EnableKafka` if needed.
  25. Kafka is typically at-least-once delivery → design consumers and status updates to be idempotent.
- **Transactions & failure handling**
  26. `@Transactional` prevents partial writes and inconsistent state.
  27. TODO: “Poison pill” message handling.
  28. DLQ pattern: `DeadLetterPublishingRecoverer` + `DefaultErrorHandler` to avoid infinite retries and enable reprocessing.