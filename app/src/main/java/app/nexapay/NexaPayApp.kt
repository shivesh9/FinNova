package app.nexapay

import android.app.Application
import app.nexapay.di.AppComponent
import app.nexapay.di.DaggerAppComponent

class NexaPayApp : Application() {
    val component: AppComponent by lazy { DaggerAppComponent.factory().create(this) }
}
