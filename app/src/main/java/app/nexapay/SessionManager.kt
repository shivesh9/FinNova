package app.nexapay

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Local-only session state for the simulation; manages dynamic user profile data. */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val _loggedIn = MutableStateFlow(prefs.getBoolean("loggedIn", false))
    val isLoggedIn: StateFlow<Boolean> = _loggedIn

    var userName: String
        get() = prefs.getString("userName", "Shivesh Verma") ?: "Shivesh Verma"
        set(value) { prefs.edit().putString("userName", value).apply() }

    var userEmail: String
        get() = prefs.getString("userEmail", "shivesh@finnova.app") ?: "shivesh@finnova.app"
        set(value) { prefs.edit().putString("userEmail", value).apply() }

    fun login(name: String? = null, email: String? = null) {
        if (!name.isNullOrBlank()) userName = name.trim()
        if (!email.isNullOrBlank()) userEmail = email.trim()
        prefs.edit().putBoolean("loggedIn", true).apply()
        _loggedIn.value = true
    }

    fun logout() {
        prefs.edit().clear().apply()
        _loggedIn.value = false
    }

    fun completeKyc(name: String? = null) {
        if (!name.isNullOrBlank()) userName = name.trim()
        prefs.edit().putString("kyc", KycStatus.VERIFIED.name).apply()
    }

    fun currentUser(): String? = if (_loggedIn.value) userEmail else null

    fun getInitials(): String {
        val parts = userName.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "FN"
        }
    }
}
