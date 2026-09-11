package app.nexapay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isLoading: Boolean = true,
    val balances: List<BalanceEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isOffline: Boolean = false,
    val error: String? = null
)

/** Presentation state is immutable and driven from Room's Flow streams. */
class HomeViewModel(repository: WalletRepository) : ViewModel() {
    val state: StateFlow<HomeUiState> = combine(repository.balances, repository.transactions) { balances, transactions ->
        HomeUiState(isLoading = false, balances = balances, recentTransactions = transactions.take(5))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
