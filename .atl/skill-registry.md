# Skill Registry — arg-bot-backend-kotlin

Generated: 2026-04-24

## Compact Rules

### java-kotlin-mentor
**Triggers**: `*.kt`, `*.java`, Spring, JPA, Reactor, Coroutines, Gradle

**Rules**:
- ALWAYS apply SOLID principles — single responsibility per class, interface segregation for ports
- Use hexagonal ports & adapters: domain NEVER imports infrastructure
- Meta-anotaciones `@UseCase`, `@WebAdapter`, `@ExternalApiAdapter` en lugar de `@Service`/`@RestController` directo
- Fallback con `runCatching { }.getOrDefault(default)` — no try/catch en use cases
- Data classes para modelos de dominio y DTOs
- `BigDecimal` para moneda — NUNCA `Double` o `Float`
- Tests con MockK (`mockk<T>()`, `every { }`, `verify { }`) — no Mockito
- `@WebMvcTest` + `@MockkBean` (springmockk) para tests de controllers
- Circuit Breaker con `@CircuitBreaker(name = "...")` de Resilience4j — no manejar en código
- RestClient (no WebClient, no RestTemplate) para HTTP calls

### architecture-review
**Triggers**: design decisions, "how to structure", new features, new adapters

**Rules**:
- Domain layer: zero framework dependencies — only stdlib and domain model
- Ports output are interfaces in domain, adapters in infrastructure
- One RestClient bean per external service (configured in RestClientConfig)
- New external API = new Adapter class implementing existing Port OR new Port + Adapter
- Never put business logic in controllers — controllers only translate HTTP ↔ domain
- `CapitalPort` is prod-only (Binance /sapi); `SpotTradingPort` works in testnet too

### sdd-apply
**Triggers**: implementing tasks from a change

**Rules**:
- STRICT TDD MODE IS ACTIVE — test command: `./gradlew test`
- Write test FIRST, then implementation
- Unit tests: MockK, no Spring context
- Integration tests: @WebMvcTest + @MockkBean
- Coverage: `./gradlew test jacocoTestReport`
- Follow existing fallback patterns (runCatching) in GetMarketDataService

### sdd-verify
**Triggers**: verifying implementation against specs

**Rules**:
- STRICT TDD MODE IS ACTIVE — test command: `./gradlew test`
- Run `./gradlew test` to verify all tests pass
- Check coverage with `./gradlew test jacocoTestReport`

---

## User Skills

| Skill | Triggers |
|-------|---------|
| `java-kotlin-mentor` | `*.kt`, `*.java`, Spring Boot, JPA, Gradle, Kotlin |
| `architecture-review` | design decisions, system structure, "how should I structure" |
| `sdd-apply` | implementing SDD tasks |
| `sdd-verify` | verifying SDD implementation |
| `sdd-explore` | investigating codebase before changes |
| `sdd-propose` | creating change proposals |
| `sdd-spec` | writing specifications |
| `sdd-design` | technical design documents |
| `sdd-tasks` | breaking down tasks |
| `sdd-archive` | archiving completed changes |
| `branch-pr` | creating pull requests |
| `issue-creation` | creating GitHub issues |
| `skill-creator` | creating new AI skills |

---

## Project Conventions

- **Package root**: `com.argbot`
- **Architecture**: Hexagonal (Ports & Adapters) + Screaming Architecture
- **Test runner**: `./gradlew test` (JUnit 5)
- **Coverage**: `./gradlew test jacocoTestReport`
- **Strict TDD**: enabled
- **Key gotcha**: `port/in/` is EMPTY — use `port/input/` for input ports
- **Key gotcha**: `BinanceOrderResponse.cummulativeQuoteQty` has intentional typo (Binance API)
- **Key gotcha**: `CapitalPort` has no testnet support — prod only
