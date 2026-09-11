# NexaPay 2.0 — demo multi-currency wallet

NexaPay is an Android portfolio project that simulates a multi-currency wallet. It never authenticates against a server, handles real identity documents, or moves real money.

## Demo flow

Use **Demo login** (the documented demo identity is `demo@nexapay.app` / `demo123`), simulate KYC verification, then explore the local wallet. Every quick action opens its own screen: Add Money and Receive credit the local INR balance, Send validates and debits the local balance before creating a pending simulated transfer, and Convert settles INR-to-USD using `BigDecimal`. The eye control hides visible monetary values, and Dark Mode is available in Profile. Data is persisted in Room and remains visible without a network connection.

## Architecture

```text
XML fragments → StateFlow / ViewModel-ready presentation → use cases → repository
                                                       ↙              ↘
                                                Room source of truth  Retrofit rate client
                                                       ↓
                                                  WorkManager sync
```

The compact single-module sample keeps domain value objects and use cases separate from Room entities, uses Dagger 2 to construct the database/repository graph, and exposes Room streams with `Flow`. The UI never reads Retrofit directly.

## Engineering choices

- **Flow rather than LiveData:** coroutine-native streams compose naturally for searches, database observations, and connectivity.
- **Room as source of truth:** screens work with cached data; a refresh updates Room rather than bypassing it.
- **BigDecimal for money:** deterministic `HALF_EVEN` rounding avoids binary floating-point errors.
- **Dagger 2:** compile-time dependency graph with no service locator.
- **Idempotency:** `transactions.idempotencyKey` has a unique Room index; repository creation returns the prior transaction for a reused key.

## Build

Open the `NexaPay` folder in Android Studio (JDK 17, Android SDK platform 35), then run `./gradlew testDebugUnitTest assembleDebug`. CI also calls the project’s `ktlint` and `detekt` verification aliases. The exchange-rate base URL is a BuildConfig value and no secret is required.

## Limitations

This intentionally compact portfolio implementation focuses on a runnable local demo. Remote rate refresh and worker scheduling are isolated extension points; no financial API, KYC provider, payment rail, or security claim is included.
