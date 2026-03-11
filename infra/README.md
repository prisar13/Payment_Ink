# Infrastructure (local)

This folder contains local infrastructure definitions for running the system during development.

## Kafka (Docker Compose)

The `docker-compose.yml` starts a single-node Kafka suitable for local dev.

### Start

From this folder:

```bash
docker compose up -d
```

Kafka will be available at:

- `localhost:9092`

### Stop

```bash
docker compose down
```

## Notes

- This is **not production** Kafka config (no replication, no HA).
- The backend services expect `KAFKA_BOOTSTRAP_SERVERS` (defaults to `localhost:9092`).
