# ARGBOT Backend

REST API that automates international transfers via **EUR → USDC → ARS** using Binance and Criptoya.

---

> [!WARNING]
> **DISCLAIMER — EDUCATIONAL PROJECT ONLY**
>
> This project is **purely educational and formative**. It was built as a learning exercise covering:
> Hexagonal Architecture, Kotlin, Spring Boot, TDD, and Clean Code principles.
>
> - It is **NOT intended for production use**
> - Do **NOT** use it with real funds or real API keys without fully understanding every line of code
> - The author takes **no responsibility** for any misuse, financial loss, or security incident arising from this project
>
> If you want to learn from it — great. If you want to automate your actual finances with it — please don't.

---

## Quick Start

```bash
./gradlew bootRun
# API available at http://localhost:10007
```

---

## What It Does

ARGBOT backend handles the server-side logic for automating cross-border transfers:

1. Fetches real-time EUR/USDC and ARS/USDC exchange rates (Binance + Criptoya P2P)
2. Executes spot trades on Binance (EUR → USDC)
3. Withdraws USDC to a BSC wallet (BEP20)
4. Exposes a REST API consumed by the ARGBOT frontend

It acts as a secure intermediary: the frontend never touches Binance API keys directly — the backend decrypts them at runtime using a shared encryption key.

---

## Tech Stack

| Category        | Technology                                |
|-----------------|-------------------------------------------|
| Language        | Kotlin 2.0.20                             |
| Framework       | Spring Boot 3.3.4                         |
| Runtime         | Java 21 (eclipse-temurin:21)              |
| Build           | Gradle (Kotlin DSL)                       |
| Testing         | JUnit 5 + MockK + SpringMockK             |
| Resilience      | Resilience4j (circuit breaker)            |
| Observability   | Spring Boot Actuator                      |
| Containerization| Docker + docker-compose                   |
| Deployment      | Render Web Service                        |

---

## Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters). The domain is fully isolated from infrastructure — it never depends on Spring, Binance, or any external concern.

```
com.argbot/
├── domain/
│   ├── model/          # TradeOrder, Withdrawal, ExchangeRate, MarketData, P2PRate...
│   └── port/
│       ├── input/      # Use case interfaces: ExecuteTradeUseCase, GetMarketDataUseCase,
│       │               # WithdrawUsdcUseCase, GetPublicIpUseCase, GetChangelogUseCase
│       └── output/     # Output ports: SpotTradingPort, CapitalPort, ExchangeRatePort,
│                       # P2PRatePort, PublicIpPort, ChangelogPort
├── application/
│   └── usecase/        # Use case implementations (services)
├── infrastructure/
│   ├── binance/        # BinanceSpotAdapter, BinanceCapitalAdapter
│   ├── criptoya/       # CriptoyaAdapter — ARS/USDC P2P rates
│   ├── ip/             # IpifyAdapter — public IP detection
│   ├── file/           # FileChangelogAdapter
│   ├── scheduler/      # SelfPingScheduler — internal keep-alive
│   └── web/            # REST controllers (ChangelogController, etc.)
└── config/             # RestClientConfig, WebConfig
```

### Flow summary

```
HTTP Request → Controller (web)
                    ↓
             Input Port (use case interface)
                    ↓
          Application Use Case (business logic)
                    ↓
             Output Port (adapter interface)
                    ↓
        Infrastructure Adapter (Binance / Criptoya / Ipify)
```

The domain layer has zero dependencies on Spring or any external library. All external calls cross the port boundary through adapters.

---

## Environment Variables

| Variable                | Required | Description                                                                                   |
|-------------------------|----------|-----------------------------------------------------------------------------------------------|
| `PORT`                  | No       | Server port. Default: `10007` (local), overridden by Render in production                    |
| `ENCRYPTION_KEY`        | Yes      | AES key used to decrypt Binance API keys at runtime. Must match `VITE_ENCRYPTION_KEY` in the frontend |
| `SPRING_PROFILES_ACTIVE`| No       | Active profile: `prod`, `docker`, or `testnet`. Omit for local development                   |

---

## Running Locally

### With Gradle

```bash
./gradlew bootRun
```

API available at `http://localhost:10007`

### With Docker

```bash
docker-compose up
```

API available at `http://localhost:10007`

---

## Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport
```

Tests are written with **JUnit 5**, **MockK**, and **SpringMockK**. Coverage report is generated under `build/reports/jacoco/`.

The test suite covers use cases and adapters in isolation — no real Binance or Criptoya calls are made during tests.

---

## API Endpoints

| Method | Path             | Description                                      |
|--------|------------------|--------------------------------------------------|
| GET    | `/api/health`    | Health check (Spring Boot Actuator)              |
| POST   | `/api/market-data` | Current EUR/USDC and ARS/USDC rates            |
| POST   | `/api/trade`     | Execute a EUR → USDC spot trade on Binance       |
| POST   | `/api/withdraw`  | Withdraw USDC to a BSC wallet (BEP20)            |
| GET    | `/api/ip`        | Public IP address of the server                  |
| GET    | `/api/changelog` | Project changelog                                |

### Circuit Breaker (Resilience4j)

Two circuit breaker instances protect external dependencies:

| Instance  | Sliding Window | Failure Rate Threshold | Wait Duration | Half-Open Calls |
|-----------|---------------|------------------------|---------------|-----------------|
| `binance`  | 5 calls       | 50%                    | 30s           | 2               |
| `criptoya` | 5 calls       | 50%                    | 30s           | 2               |

---

## Deployment

The backend runs on **Render** as a Web Service using the free tier.

### Keep-Alive

Render's free tier spins down instances after ~15 minutes of inactivity. A GitHub Actions workflow (`.github/workflows/keepalive.yml`) pings `/api/health` every ~9 minutes to keep the service warm.

### Spring Profiles

| Profile file                      | Purpose                                         |
|-----------------------------------|-------------------------------------------------|
| `application.yml`                 | Local base config (port 10007, all Actuator endpoints) |
| `application.properties`          | Production override (port from `PORT` env, only `/health`) |
| `application-testnet.properties`  | Testnet mode (safe for experimentation)         |

---

## License

This project is open source. See the repository on GitHub for the full source code and history.
