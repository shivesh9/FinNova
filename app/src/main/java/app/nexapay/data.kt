package app.nexapay

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.UUID

@Entity(tableName = "balances", primaryKeys = ["currency"])
data class BalanceEntity(val currency: String, val amount: String)

@Entity(tableName = "users") data class UserEntity(@androidx.room.PrimaryKey val id: String, val email: String, val displayName: String)
@Entity(tableName = "kyc") data class KycEntity(@androidx.room.PrimaryKey val userId: String, val status: String, val updatedAt: Long)
@Entity(tableName = "wallets") data class WalletEntity(@androidx.room.PrimaryKey val id: String, val userId: String, val displayCurrency: String)
@Entity(tableName = "exchange_rates", primaryKeys = ["base", "quote"]) data class ExchangeRateEntity(val base: String, val quote: String, val rate: String, val updatedAt: Long)

@Entity(tableName = "transactions", indices = [Index("idempotencyKey", unique = true), Index("createdAt"), Index("category"), Index("status")])
data class TransactionEntity(
    @androidx.room.PrimaryKey val transactionId: String = UUID.randomUUID().toString(),
    val idempotencyKey: String,
    val type: String,
    val status: String,
    val amount: String,
    val currency: String,
    val category: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
)

@Entity(tableName = "beneficiaries")
data class BeneficiaryEntity(@androidx.room.PrimaryKey val id: String, val name: String, val accountIdentifier: String, val currency: String, val nickname: String)

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(@androidx.room.PrimaryKey val id: String, val operationType: String, val payload: String, val status: String, val retryCount: Int, val createdAt: Long)

@Dao interface WalletDao {
    @Query("SELECT * FROM balances ORDER BY currency") fun observeBalances(): Flow<List<BalanceEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveBalances(items: List<BalanceEntity>)
    @Query("SELECT * FROM balances WHERE currency = :currency LIMIT 1") suspend fun balance(currency: String): BalanceEntity?
    @Query("SELECT COUNT(*) FROM balances") suspend fun balanceCount(): Int
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC") fun observeTransactions(): Flow<List<TransactionEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertTransaction(item: TransactionEntity): Long
    @Query("SELECT * FROM transactions WHERE idempotencyKey = :key LIMIT 1") suspend fun findByIdempotencyKey(key: String): TransactionEntity?
    @Query("UPDATE transactions SET status = :status, updatedAt = :updatedAt WHERE transactionId = :id") suspend fun updateStatus(id: String, status: String, updatedAt: Long)
}

@Database(entities = [UserEntity::class, KycEntity::class, WalletEntity::class, BalanceEntity::class, TransactionEntity::class, BeneficiaryEntity::class, ExchangeRateEntity::class, PendingOperationEntity::class], version = 2, exportSchema = true)
abstract class NexaPayDatabase : RoomDatabase() { abstract fun walletDao(): WalletDao }

class WalletRepository(private val dao: WalletDao) {
    val balances: Flow<List<BalanceEntity>> = dao.observeBalances()
    val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()
    suspend fun seed() {
        if (dao.balanceCount() > 0) return
        dao.saveBalances(listOf("INR" to "185000.00", "USD" to "500.00", "EUR" to "300.00", "GBP" to "250.00", "AED" to "1000.00", "SGD" to "200.00").map { BalanceEntity(it.first, it.second) })
        listOf("Salary Simulation" to "85000.00", "Coffee Shop" to "-280.00", "USD Conversion" to "-120.00", "Demo Transfer" to "-5000.00").forEach { (name, amount) -> create(TransactionEntity(idempotencyKey = "seed-$name", type = "TRANSFER", status = "COMPLETED", amount = amount, currency = "INR", category = "Demo", description = name)) }
    }
    suspend fun create(transaction: TransactionEntity): TransactionEntity = dao.findByIdempotencyKey(transaction.idempotencyKey) ?: run { dao.insertTransaction(transaction); transaction }
    suspend fun addDemoFunds(amount: BigDecimal) {
        adjustBalance("INR", amount)
        create(TransactionEntity(idempotencyKey = UUID.randomUUID().toString(), type = "ADD_MONEY", status = "COMPLETED", amount = amount.toPlainString(), currency = "INR", category = "Demo", description = "Demo funds added — no real money"))
    }
    suspend fun sendDemoMoney(amount: BigDecimal, recipient: String): TransactionEntity {
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
        val balance = dao.balance("INR")?.amount?.toBigDecimal() ?: BigDecimal.ZERO
        require(balance >= amount) { "Insufficient demo balance" }
        adjustBalance("INR", amount.negate())
        return create(TransactionEntity(idempotencyKey = UUID.randomUUID().toString(), type = "TRANSFER", status = "PENDING", amount = amount.negate().toPlainString(), currency = "INR", category = "Transfer", description = "Demo transfer to $recipient"))
    }
    suspend fun receiveDemoMoney(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
        adjustBalance("INR", amount)
        create(TransactionEntity(idempotencyKey = UUID.randomUUID().toString(), type = "RECEIVE", status = "COMPLETED", amount = amount.toPlainString(), currency = "INR", category = "Income", description = "Demo payment received"))
    }
    suspend fun convertInrToUsd(amount: BigDecimal, receive: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
        val balance = dao.balance("INR")?.amount?.toBigDecimal() ?: BigDecimal.ZERO
        require(balance >= amount) { "Insufficient demo balance" }
        adjustBalance("INR", amount.negate()); adjustBalance("USD", receive)
        create(TransactionEntity(idempotencyKey = UUID.randomUUID().toString(), type = "CONVERSION", status = "COMPLETED", amount = receive.toPlainString(), currency = "USD", category = "Conversion", description = "INR to USD demo conversion"))
    }
    private suspend fun adjustBalance(currency: String, delta: BigDecimal) {
        val current = dao.balance(currency) ?: BalanceEntity(currency, "0.00")
        dao.saveBalances(listOf(current.copy(amount = current.amount.toBigDecimal().add(delta).setScale(2).toPlainString())))
    }
}
