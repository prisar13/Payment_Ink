# Payment + Fraud Detection (microservices)

End-to-end demo of a payment workflow with asynchronous fraud evaluation, built as a small distributed system:

- `payment_service` (Spring Boot): transaction creation, auth/JWT, publishes events to Kafka, exposes APIs consumed by the UI.
- `Sentinal` (Spring Boot): fraud engine that consumes Kafka events, evaluates transactions, and sends status callbacks (with retry/backoff).
- `fraud-ui` (React + Vite): simple UI for login and viewing transactions/alerts.
- `infra` (Docker Compose): local Kafka (and related infra as the project grows).

## Architecture (high level)

1. User calls `payment_service` to create a transaction.
2. `payment_service` publishes `transaction.created` to Kafka.
3. `Sentinal` consumes the event, runs fraud evaluation, and decides (approve / decline / review).
4. `Sentinal` calls back into `payment_service` to update transaction status. If `payment_service` is down, it retries with exponential backoff.

## Repository layout

- `payment_service/` – Spring Boot (Java 17, Spring Boot 3.2.x)
- `Sentinal/` – Spring Boot (Java 17, Spring Boot 3.3.x)
- `fraud-ui/` – React (Vite)
- `infra/` – Docker Compose (Kafka)
- `docs/` – Documentation (templates + project notes)

## Tech stack

- **Backend**: Spring Boot, Spring Security (JWT), Spring Data JPA, Flyway, Kafka
- **Data**: PostgreSQL
- **Cache/queue**: Redis (Sentinal), Kafka for events
- **Frontend**: React, React Router, Axios, Vite

## Prerequisites

- Java 17
- Maven (or use the included Maven wrapper)
- Node.js (for `fraud-ui`)
- Docker Desktop (recommended for Kafka via `infra/docker-compose.yml`)
- PostgreSQL (local install or container)
- Redis (local install or container)

## Local configuration (no secrets in git)

This repo is set up so that **passwords/secrets are not committed**.

- Copy `.env.example` to `.env` and fill in local values.
- For private notes you want to keep but never publish:
  - Create `docs/private/PRIVATE_NOTES.md` (template is committed at `docs/private/PRIVATE_NOTES.template.md`).

The Spring services read credentials from environment variables (see `.env.example` for the list).

## Quickstart (typical dev flow)

1. Start infrastructure:
   - Kafka: see `infra/README.md`
   - Start PostgreSQL databases:
     - `payment_service` DB: `payment_service`
     - `Sentinal` DB: `fraud_engine`
   - Start Redis (for `Sentinal`)
2. Run backend services:
   - `Sentinal` on `:8080`
   - `payment_service` on `:8081`
3. Run the UI:
   - `fraud-ui` on `:5173` (default Vite)

## Service ports & URLs

- `Sentinal` (fraud engine): `http://localhost:8080`
- `payment_service`: `http://localhost:8081`
- `fraud-ui`: `http://localhost:5173`
- Kafka: `localhost:9092`
- Redis: `localhost:6379`

## API overview (most used)

### payment_service (`:8081`)

- `POST /auth/register`
- `POST /auth/login`
- `POST /transaction/create`
- `POST /transaction/statusUpdate` (service callback)
- `GET /transaction?page=0&size=10`
- `GET /fraud/alerts?page=0&size=10` (proxy to Sentinal)

### Sentinal (`:8080`)

- `POST /evaluate`
- `GET /alerts?page=0&size=10`

## Security & secret hygiene

- **Never commit**: DB passwords, admin passwords, JWT signing keys, service tokens, `.env` files, or any private notes.
- If secrets were committed previously and pushed to GitHub, removing them from the latest commit is not enough—you must also remove them from git history. See “Remediation” below.

### Remediation (if something was already pushed)

If credentials were committed at any point:

1. **Rotate the credential** (assume it is compromised).
2. **Rewrite git history** to remove it (example approaches):
   - `git filter-repo` (recommended)
   - BFG Repo-Cleaner
3. Force-push the cleaned history (only after coordinating with anyone else using the repo).

This repo already replaces hardcoded passwords in `application.properties` with env vars so future commits don’t re-leak the same values.

## Notes & lessons learned

The subproject READMEs include a curated “Notes / lessons” section with the key takeaways from building the system (Spring Security gotchas, idempotency, retry patterns, Kafka delivery semantics, etc.).
