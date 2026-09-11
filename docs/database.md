# Database

Room holds balances, transactions, beneficiaries, and pending operations. Transactions index idempotency key, timestamp, category, and status. The unique idempotency index is the final guard against duplicate money operations.
