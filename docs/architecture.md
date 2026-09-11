# Architecture

NexaPay uses a single activity with XML-backed fragments. Fragment events call narrow use cases/repository methods; Room flows drive rendered balances and transactions. Dagger owns the database and repository lifetime. The `Money` value object keeps a `BigDecimal` with its currency and rejects cross-currency arithmetic.

The local database is the source of truth. Rate refreshes should map Retrofit DTOs to local cached entities; failed refreshes retain cached values and the UI can show offline state.
