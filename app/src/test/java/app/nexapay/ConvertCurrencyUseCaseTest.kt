package app.nexapay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency

class ConvertCurrencyUseCaseTest {
    private val inr = Currency.getInstance("INR")
    @Test fun `conversion applies fee before deterministic half even rounding`() {
        val quote = ConvertCurrencyUseCase()(Money(BigDecimal("10000.00"), inr), Currency.getInstance("USD"), BigDecimal("0.0119875"), Money(BigDecimal("50.00"), inr))
        assertEquals(BigDecimal("119.28"), quote.receive.amount)
        assertEquals(BigDecimal("50.00"), quote.fee.amount)
    }
    @Test fun `money rejects addition across currencies`() {
        val result = runCatching { Money(BigDecimal("1.00"), inr) + Money(BigDecimal("1.00"), Currency.getInstance("USD")) }
        assertTrue(result.isFailure)
    }
}
