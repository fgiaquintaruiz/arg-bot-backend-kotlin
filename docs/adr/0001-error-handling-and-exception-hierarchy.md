# ADR 0001 — Error handling and exception hierarchy

## Status
Accepted (2026-05-09)

## Context

The backend had no centralized error handling. Each controller implemented its own try-catch strategy:
- `TradeController` caught `Exception` generically, returning 400 for all errors including circuit breaker open states
- `WithdrawController` used `e.message` directly as the error body
- Circuit breakers (Resilience4j) propagated `CallNotPermittedException` without HTTP-level distinction from client errors

Additionally, external provider exceptions (`BinanceApiException`, `CriptoyaApiException`, `IpifyApiException`) were independent `RuntimeException` subtypes with no shared contract, requiring separate `@ExceptionHandler` registrations per type.

## Decision

### 1. Centralized error handling via `@ControllerAdvice`

`GlobalExceptionHandler` maps exception types to HTTP status codes and a structured error body.

Error response format: `{ error: String, code: String, retryAfter: Int? }`

HTTP mapping:

| Exception | HTTP Status | code |
|-----------|-------------|------|
| `CallNotPermittedException` | 503 Service Unavailable | `CIRCUIT_OPEN` |
| `ExternalProviderException` (any subtype) | 502 Bad Gateway | per subtype |
| `IllegalArgumentException` | 400 Bad Request | `BAD_REQUEST` |
| `Exception` (fallback) | 500 Internal Server Error | `INTERNAL_ERROR` |

Circuit breaker responses include `Retry-After: 30` header.

### 2. Abstract exception hierarchy for external providers

`ExternalProviderException` is an `abstract class` (not `sealed`) that carries `code` and `userMessage`.

Originally planned as `sealed` to enforce exhaustive `when` dispatch, but Kotlin 2.0.20 requires all subtypes of a `sealed class` to be in the same package. Since `BinanceApiException`, `CriptoyaApiException`, and `IpifyApiException` reside in their respective infrastructure packages, `abstract class` was chosen.

Trade-off: loses `when` exhaustiveness guarantees, gains package flexibility. Acceptable because no current code dispatches on exception subtype via `when` — the handler uses polymorphism through `code` and `userMessage` fields.

Subtypes:
- `BinanceApiException` → code `BINANCE_ERROR`
- `CriptoyaApiException` → code `CRIPTOYA_ERROR`, fixed userMessage
- `IpifyApiException` → code `IPIFY_ERROR`, fixed userMessage

### 3. Circuit breaker fallback strategy

Three categories define the fallback approach:

- **Category 1 (writes, money movement)**: no fallback. `placeMarketOrder`, `submitWithdrawal` — if the CB is open, the operation must not silently succeed or silently fail. Propagate → 503.
- **Category 2 (critical reads: prices, balances, fees, public IP)**: no fallback. Data is required for correct operation; a stale or null value would be worse than an explicit error. Propagate → 503.
- **Category 3 (non-critical info where partial response is acceptable)**: fallback returns `null` or a sentinel. No current endpoint qualifies — IpifyAdapter was evaluated and assigned Category 2 (single-field response; `null` IP would make the response useless).

Default: no `fallbackMethod` unless explicitly justified per endpoint.

## Consequences

### Positive
- Consistent error format across all endpoints
- Circuit breaker open → 503 (not 400), enabling client-side retry logic
- Single `@ExceptionHandler` covers all external provider exceptions
- `BinanceErrorParser` isolated from web layer (moved from TradeController to `infrastructure/binance/`)

### Negative
- `abstract class` loses compile-time exhaustiveness on `when` dispatch
- New external provider exceptions must manually extend `ExternalProviderException`; there is no compiler enforcement

### Deferred
- Full DDD exception hierarchy: `DomainException → InfrastructureException → ExternalProviderException` — deferred until a domain-level exception use case emerges
- Structured logging / OpenTelemetry tracing per exception type — deferred to observability tanda
