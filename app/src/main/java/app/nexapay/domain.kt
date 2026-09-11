package app.nexapay

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

data class Money(val amount: BigDecimal, val currency: Currency) {
    init { require(amount.scale() <= currency.defaultFractionDigits.coerceAtLeast(2)) { "Invalid money scale" } }
    operator fun plus(other: Money): Money { require(currency == other.currency); return copy(amount = amount + other.amount) }
    operator fun minus(other: Money): Money { require(currency == other.currency); return copy(amount = amount - other.amount) }
    fun format(): String = "${currency.symbol}${amount.setScale(2, RoundingMode.HALF_EVEN)}"
}

data class ConversionQuote(val receive: Money, val fee: Money, val rate: BigDecimal)

class ConvertCurrencyUseCase {
    operator fun invoke(amount: Money, target: Currency, targetPerSource: BigDecimal, fee: Money): ConversionQuote {
        require(amount.amount > BigDecimal.ZERO && fee.currency == amount.currency)
        val afterFee = (amount.amount - fee.amount).coerceAtLeast(BigDecimal.ZERO)
        return ConversionQuote(Money(afterFee.multiply(targetPerSource).setScale(2, RoundingMode.HALF_EVEN), target), fee, targetPerSource)
    }
}

enum class TransactionStatus { PENDING, COMPLETED, FAILED }
enum class TransactionType { TRANSFER, RECEIVE, CONVERSION, ADD_MONEY, FEE }
enum class KycStatus { PENDING, VERIFIED, REJECTED }
