# Offline sync

UI reads Room only. User actions are first committed locally and can be represented in `pending_operations`; a `WorkManager` job retries them after connectivity returns. This demo resolves pending operations against local simulated state, never a payment provider.
