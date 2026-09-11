package app.nexapay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.nexapay.databinding.FragmentConvertBinding
import app.nexapay.databinding.FragmentActionBinding
import app.nexapay.databinding.FragmentHomeBinding
import app.nexapay.databinding.FragmentKycBinding
import app.nexapay.databinding.FragmentOnboardingBinding
import app.nexapay.databinding.FragmentProfileBinding
import app.nexapay.databinding.FragmentTransactionsBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

private fun Fragment.repository() = (requireActivity().application as NexaPayApp).component.repository()

class OnboardingFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentOnboardingBinding.inflate(i, c, false)
        b.login.setOnClickListener { (activity as MainActivity).login() }; b.create.setOnClickListener { (activity as MainActivity).login() }
        return b.root
    }
}
class KycFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentKycBinding.inflate(i, c, false)
        val mainActivity = activity as? MainActivity
        mainActivity?.session?.let { session ->
            b.fullName.setText(session.userName)
        }
        b.verify.setOnClickListener {
            val enteredName = b.fullName.text?.toString()?.trim()
            Toast.makeText(requireContext(), "Verification complete", Toast.LENGTH_SHORT).show()
            mainActivity?.verified(enteredName)
        }
        return b.root
    }
}
class HomeFragment : Fragment() {
    private var balanceHidden = false
    private var latestBalances: List<BalanceEntity> = emptyList()
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentHomeBinding.inflate(i, c, false); val repo = repository()
        val mainActivity = activity as? MainActivity

        renderGreeting(b, mainActivity)

        lifecycleScope.launch { repo.seed() }
        lifecycleScope.launch { repo.balances.collect { balances -> latestBalances = balances; renderBalances(b) } }
        lifecycleScope.launch { repo.transactions.collect { tx -> b.recent.text = tx.take(4).joinToString("\n") { "${it.description}  ${it.amount}" } } }
        b.addFunds.setOnClickListener { (activity as MainActivity).openAction("add money") }
        b.send.setOnClickListener { (activity as MainActivity).openAction("send money") }
        b.convertAction.setOnClickListener { (activity as MainActivity).openTab(R.id.convert) }
        b.receive.setOnClickListener { (activity as MainActivity).openAction("receive money") }
        b.eye.setOnClickListener { balanceHidden = !balanceHidden; renderBalances(b) }
        return b.root
    }
    private fun renderGreeting(b: FragmentHomeBinding, mainActivity: MainActivity?) {
        val session = mainActivity?.session
        val fullName = session?.userName ?: "Shivesh Verma"
        val firstName = fullName.split(" ").firstOrNull { it.isNotBlank() } ?: fullName
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val timeOfDay = when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Welcome back"
        }
        b.greeting.text = "$timeOfDay, $firstName"
        b.userAvatar.text = session?.getInitials() ?: "SV"
    }
    private fun renderBalances(b: FragmentHomeBinding) {
        b.eye.text = if (balanceHidden) "◌" else "◉"
        b.total.text = if (balanceHidden) "₹ ••••••" else "₹${latestBalances.firstOrNull { it.currency == "INR" }?.amount ?: "0.00"}"
        b.balanceSubtitle.text = if (balanceHidden) "Balance hidden • Tap the eye to reveal" else "≈ $2,940.22 USD   •   Updated just now"
        b.balances.text = latestBalances.joinToString("\n") { "${it.currency.padEnd(4)} ${if (balanceHidden) "••••••" else "${symbol(it.currency)}${it.amount}"}" }
    }
    private fun symbol(code: String) = when (code) { "INR" -> "₹"; "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "AED" -> "د.إ"; else -> "S$" }
}
class ConvertFragment : Fragment() {
    private val currencies = arrayOf("USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD", "CHF")

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentConvertBinding.inflate(i, c, false)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        (b.fromCurrencyDropdown as? AutoCompleteTextView)?.setAdapter(adapter)
        (b.toCurrencyDropdown as? AutoCompleteTextView)?.setAdapter(adapter)

        b.fromCurrencyDropdown.setText("USD", false)
        b.toCurrencyDropdown.setText("INR", false)

        b.swapButton.setOnClickListener {
            val from = b.fromCurrencyDropdown.text.toString()
            val to = b.toCurrencyDropdown.text.toString()
            b.fromCurrencyDropdown.setText(to, false)
            b.toCurrencyDropdown.setText(from, false)
        }

        b.convertButton.setOnClickListener {
            performConversion(b)
        }

        return b.root
    }

    private fun performConversion(b: FragmentConvertBinding) {
        val amountStr = b.amount.text?.toString()?.trim()
        val amount = amountStr?.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            b.amountInputLayout.error = "Please enter a valid positive amount"
            b.resultCard.visibility = View.GONE
            b.errorCard.visibility = View.GONE
            return
        } else {
            b.amountInputLayout.error = null
        }

        val from = b.fromCurrencyDropdown.text.toString().trim()
        val to = b.toCurrencyDropdown.text.toString().trim()

        if (from == to) {
            b.resultCard.visibility = View.VISIBLE
            b.errorCard.visibility = View.GONE
            b.convertedAmountText.text = String.format(java.util.Locale.US, "%.2f %s", amount, to)
            b.exchangeRateText.text = "1 $from = 1.00 $to"
            b.rateLastUpdatedText.text = "Identical currencies selected"
            return
        }

        b.progressBar.visibility = View.VISIBLE
        b.convertButton.isEnabled = false
        b.resultCard.visibility = View.GONE
        b.errorCard.visibility = View.GONE

        lifecycleScope.launch {
            var rate: Double? = null
            var resultVal: Double? = null
            var timeUpdated: String? = null
            var errorMsg: String? = null

            val apiKey = BuildConfig.EXCHANGE_RATE_API_KEY
            val isCustomKey = apiKey.isNotBlank() && apiKey != "YOUR_API_KEY"

            if (isCustomKey) {
                try {
                    val keyResponse = NetworkClient.keyApi.convertPairWithKey(apiKey, from, to, amount)
                    if (keyResponse.result == "success" && keyResponse.conversionResult != null) {
                        rate = keyResponse.conversionRate
                        resultVal = keyResponse.conversionResult
                        timeUpdated = keyResponse.timeLastUpdateUtc
                    } else if (keyResponse.errorType != null) {
                        errorMsg = "API Error: ${keyResponse.errorType}"
                    }
                } catch (e: Exception) {
                    // Fall back to free API if key API call fails
                }
            }

            if (resultVal == null && errorMsg == null) {
                try {
                    val freeResponse = NetworkClient.freeApi.latestFree(from)
                    if (freeResponse.result == "success" && freeResponse.rates != null) {
                        val fetchedRate = freeResponse.rates[to]
                        if (fetchedRate != null) {
                            rate = fetchedRate
                            resultVal = amount * fetchedRate
                            timeUpdated = freeResponse.timeLastUpdateUtc ?: "Just now"
                        } else {
                            errorMsg = "Unsupported target currency $to"
                        }
                    } else {
                        errorMsg = "Unable to fetch rates from ExchangeRate-API"
                    }
                } catch (e: Exception) {
                    errorMsg = when (e) {
                        is java.net.UnknownHostException -> "Network error: Unable to connect. Please check your internet connection."
                        else -> "Failed to fetch rates: ${e.localizedMessage ?: "Network error"}"
                    }
                }
            }

            if (resultVal != null && rate != null) {
                b.convertedAmountText.text = String.format(java.util.Locale.US, "%,.2f %s", resultVal, to)
                b.exchangeRateText.text = String.format(java.util.Locale.US, "1 %s = %,.4f %s", from, rate, to)
                b.rateLastUpdatedText.text = "Last updated: ${timeUpdated ?: "Just now"}"

                b.resultCard.visibility = View.VISIBLE
                b.errorCard.visibility = View.GONE
            } else {
                b.errorText.text = errorMsg ?: "Unable to complete currency conversion."
                b.errorCard.visibility = View.VISIBLE
                b.resultCard.visibility = View.GONE
            }

            b.progressBar.visibility = View.GONE
            b.convertButton.isEnabled = true
        }
    }
}
class ActionFragment : Fragment() {
    private val type by lazy { requireArguments().getString("type") ?: "add money" }
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentActionBinding.inflate(i, c, false)
        val isSend = type == "send money"
        b.eyebrow.text = "FINNOVA • DEMO ACTION"
        b.title.text = when (type) { "send money" -> "Send money"; "receive money" -> "Receive money"; else -> "Add demo funds" }
        b.subtitle.text = when (type) { "send money" -> "Choose a recipient and review your simulated transfer."; "receive money" -> "Create a local incoming payment simulation."; else -> "Top up your local wallet with demo funds." }
        b.recipientLayout.visibility = if (isSend) View.VISIBLE else View.GONE
        if (isSend) b.recipient.setText("Rahul Mehta")
        b.info.text = when (type) { "send money" -> "Recipient: local demo beneficiary\nFee: ₹0.00 • Status: pending then completed"; "receive money" -> "Your demo receive ID: FINNOVA-ALEX-2048\nIncoming funds appear instantly in this local wallet."; else -> "Funds are local demo data only. No payment method is charged." }
        b.continueButton.text = when (type) { "send money" -> "Review & send"; "receive money" -> "Simulate receipt"; else -> "Add demo funds" }
        b.continueButton.setOnClickListener {
            val amount = b.amount.text?.toString()?.toBigDecimalOrNull()
            if (amount == null || amount <= BigDecimal.ZERO) { b.amount.error = "Enter a valid amount"; return@setOnClickListener }
            lifecycleScope.launch {
                val operation = runCatching { when (type) { "send money" -> repository().sendDemoMoney(amount, b.recipient.text?.toString().orEmpty().ifBlank { "Demo recipient" }); "receive money" -> repository().receiveDemoMoney(amount); else -> repository().addDemoFunds(amount) } }
                operation.onSuccess { Toast.makeText(requireContext(), when (type) { "send money" -> "Transfer created and pending"; "receive money" -> "Demo payment received"; else -> "Demo funds added" }, Toast.LENGTH_LONG).show(); (activity as MainActivity).openTab(R.id.home) }
                    .onFailure { Toast.makeText(requireContext(), it.message ?: "Could not complete demo action", Toast.LENGTH_LONG).show() }
            }
        }
        return b.root
    }
    companion object { fun newInstance(type: String) = ActionFragment().apply { arguments = Bundle().apply { putString("type", type) } } }
}
class TransactionsFragment : Fragment() {
    private val query = MutableStateFlow("")
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentTransactionsBinding.inflate(i, c, false); val adapter = TransactionAdapter(); b.list.layoutManager = LinearLayoutManager(requireContext()); b.list.adapter = adapter
        b.search.doAfterTextChanged { query.value = it?.toString().orEmpty() }
        lifecycleScope.launch { combine(repository().transactions, query.debounce(250)) { tx, q -> tx.filter { it.description.contains(q, true) || it.category.contains(q, true) } }.collect(adapter::submit) }
        return b.root
    }
}
class ProfileFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentProfileBinding.inflate(i, c, false)
        val mainActivity = activity as? MainActivity
        mainActivity?.session?.let { session ->
            b.profileName.text = session.userName
            b.profileEmail.text = "${session.userEmail}   •   Verified"
        }
        b.darkMode.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        b.darkMode.setOnCheckedChangeListener { _, checked -> AppCompatDelegate.setDefaultNightMode(if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO) }
        b.logout.setOnClickListener { (activity as MainActivity).logout() }
        return b.root
    }
}
private class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.Holder>() {
    private var items: List<TransactionEntity> = emptyList()
    fun submit(value: List<TransactionEntity>) { items = value; notifyDataSetChanged() }
    class Holder(val text: TextView) : RecyclerView.ViewHolder(text)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(TextView(parent.context).apply { setPadding(8, 20, 8, 20); textSize = 16f })
    override fun onBindViewHolder(holder: Holder, position: Int) { val t = items[position]; holder.text.text = "${t.description}\n${t.category} • ${t.status}                                      ${t.amount} ${t.currency}" }
    override fun getItemCount() = items.size
}
