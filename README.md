# PulseGate

PulseGate is a small full-stack API rate limiter and request inspector built with Java, Spring Boot, H2, HTML, CSS, and JavaScript.

It lets a developer generate an API key, call a protected endpoint, simulate a traffic burst, and inspect request behavior through an API reliability dashboard.

## Features

- Generate API keys with owner names.
- Protect `GET /api/protected` with the `X-API-Key` header.
- Limit each API key to 10 requests per minute.
- Return `429 Too Many Requests` when a key exceeds the limit.
- Persist request logs with status code, latency, IP address, timestamp, and blocked status.
- Show dashboard metrics for total requests, successes, errors, blocked requests, and average latency.
- Run a frontend traffic burst that sends 20 requests quickly.
- Expose H2 console and Spring Boot Actuator health endpoint for local development.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Boot Actuator
- H2 Database
- HTML, CSS, JavaScript
- Maven

## Run Locally

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080
```

H2 console:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:pulsegate
User: sa
Password: password
```

## Deploy Online

The app reads the hosting platform's `PORT` environment variable, so it can run locally on `8080` and online on the port assigned by the platform.

### Railway

1. Push this project to a GitHub repository.
2. In Railway, create a new project.
3. Choose **Deploy from GitHub repo**.
4. Select the PulseGate repository.
5. After deployment finishes, go to **Settings > Networking** and generate a public domain.

### Render

1. Push this project to a GitHub repository.
2. In Render, create a new **Web Service**.
3. Connect the PulseGate repository.
4. Use Docker deployment. Render will build from the included `Dockerfile`.
5. Open the generated `onrender.com` URL.

Note: PulseGate uses an in-memory H2 database for demo simplicity. On free/demo hosting, data resets when the app restarts. For a production version, connect PostgreSQL.

## API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/keys` | Create an API key |
| `GET` | `/api/keys` | List API keys and usage |
| `GET` | `/api/protected` | Protected endpoint requiring `X-API-Key` |
| `GET` | `/api/dashboard/summary` | Dashboard metrics |
| `GET` | `/api/logs` | Recent request logs |
| `GET` | `/api/rate-limit/{apiKey}/status` | Rate-limit status for a key |
| `DELETE` | `/api/demo-data` | Clear demo API keys and request logs |
| `GET` | `/actuator/health` | App health |

## Example API Key Request

```bash
curl -X POST http://localhost:8080/api/keys \
  -H "Content-Type: application/json" \
  -d '{"ownerName":"Amrinder"}'
```

## Example Protected Request

```bash
curl http://localhost:8080/api/protected \
  -H "X-API-Key: pg_live_your_key_here"
```

## Resume Bullet

Built a full-stack API rate limiting and request inspection dashboard using Java, Spring Boot, SQL, HTML, CSS, and JavaScript, with API key generation, protected request handling, SQL-backed request logs, dashboard analytics, and a traffic-burst workflow that demonstrates `429 Too Many Requests` behavior.
