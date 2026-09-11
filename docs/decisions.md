# Architecture decision records

## ADR-001 Clean architecture
**Context:** UI and persistence must evolve independently. **Decision:** domain values/use cases sit between presentation and data. **Alternative:** fragments talking to DAOs. **Trade-off:** more small types.

## ADR-002 Room source of truth
**Context:** wallet must be readable offline. **Decision:** Room drives every visible stream. **Alternative:** network-first screens. **Trade-off:** mapping and migrations.

## ADR-003 Kotlin Flow
**Context:** balances, transactions, search, and connectivity change over time. **Decision:** coroutine Flow. **Alternative:** LiveData. **Trade-off:** lifecycle collection discipline.

## ADR-004 Dagger 2
**Context:** dependencies should be explicit and testable. **Decision:** Dagger component/module graph. **Alternative:** service locator. **Trade-off:** generated code.

## ADR-005 BigDecimal
**Context:** financial rounding must be stable. **Decision:** explicit `BigDecimal` and `HALF_EVEN`. **Alternative:** Double. **Trade-off:** verbosity.

## ADR-006 Offline synchronization
**Context:** simulated operations should survive a lost network. **Decision:** durable pending-operation records and WorkManager. **Alternative:** in-memory retries. **Trade-off:** eventual consistency.

## ADR-007 Demo authentication
**Context:** this is a portfolio app, not a bank. **Decision:** local session and simulated KYC. **Alternative:** OAuth/Firebase. **Trade-off:** no real identity assurance by design.
