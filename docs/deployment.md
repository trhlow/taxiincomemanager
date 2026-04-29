# Deployment

This project is structured for **small-scale** or **demo** deployment. There is no baked-in Kubernetes chart; use any JVM + Postgres host.

## Prerequisites

- **PostgreSQL 16+** reachable from the API.
- **Java 21** runtime for the Spring Boot fat JAR.
- Environment variables / config for:
  - `SPRING_DATASOURCE_*` (URL, user, password)
  - `APP_API_KEY` — must match what mobile apps send as `X-Api-Key` / `TAXI_API_KEY`.

Build the backend:

```bash
cd backend
./mvnw -q -DskipTests package
java -jar target/*.jar
```

Flyway migrations run automatically on startup.

## Mobile builds

Release builds **require** `--dart-define=TAXI_API_KEY=<same as server APP_API_KEY>` (see README). Point the app base URL at your HTTPS ingress or internal IP.

## Docker Compose (development only)

Root `docker-compose.yml` starts Postgres for local developers. Health checks use `$POSTGRES_USER` / `$POSTGRES_DB` so the container reports healthy whenever credentials match `.env`.

## TLS

Terminate TLS at Nginx, Caddy, or a cloud load balancer; forward plain HTTP to the Spring Boot port only on a trusted network segment.
