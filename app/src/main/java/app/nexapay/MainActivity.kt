package app.nexapay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import app.nexapay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val session by lazy { SessionManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(0, statusBarInsets.top, 0, navBarInsets.bottom)
            insets
        }

        binding.navigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> show(HomeFragment(), "Home")
                R.id.convert -> show(ConvertFragment(), "Convert")
                R.id.transactions -> show(TransactionsFragment(), "Transactions")
                R.id.profile -> show(ProfileFragment(), "Profile")
                else -> return@setOnItemSelectedListener false
            }
            true
        }

        if (session.isLoggedIn.value) {
            binding.navigation.selectedItemId = R.id.home
        } else {
            show(OnboardingFragment(), "FinNova")
        }
    }

    fun login(name: String? = null) { session.login(name); show(KycFragment(), "Verify identity") }
    fun verified(name: String? = null) { session.completeKyc(name); binding.navigation.selectedItemId = R.id.home }
    fun logout() { session.logout(); show(OnboardingFragment(), "FinNova") }
    fun openTab(id: Int) { binding.navigation.selectedItemId = id }
    fun openAction(type: String) { show(ActionFragment.newInstance(type), type.replaceFirstChar { it.uppercase() }) }
    fun show(fragment: Fragment, title: String) {
        binding.toolbar.title = title
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment).commit()
        binding.navigation.visibility = if (fragment is OnboardingFragment || fragment is KycFragment || fragment is ActionFragment) android.view.View.GONE else android.view.View.VISIBLE
    }
}
