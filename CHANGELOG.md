# Changelog - ArgBot Backend (Kotlin)

## [0.1.0] - 2026-04-23
### Added
- **Screaming Architecture**: Refactored project structure to focus on business use cases (Hexagonal Architecture).
- **Market Data**: Ported /api/data from Node.js with support for Binance and Criptoya.
- **Trading**: Ported /api/trade for EUR/USDC market orders on Binance.
- **Withdrawals**: Ported /api/withdraw for USDC withdrawals via BSC network.
- **Public IP**: Added /api/ip endpoint using Ipify with Resilience4j Circuit Breaker.
- **Changelog**: Added /api/changelog endpoint to serve this file.
- **Resilience**: Integrated Resilience4j for all external API calls.
- **Security**: Ported AES decryption for Binance API keys.
- **Configuration**: Dynamic RestClient setup with @Qualifier for multiple external services.
- **Profiles**: Added 	estnet and prod Spring profiles.

### Fixed
- **Dependency Injection**: Fixed UnsatisfiedDependencyException by adding @Qualifier to RestClient beans.
