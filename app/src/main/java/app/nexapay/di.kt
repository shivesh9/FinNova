package app.nexapay.di

import android.content.Context
import androidx.room.Room
import app.nexapay.NexaPayDatabase
import app.nexapay.WalletDao
import app.nexapay.WalletRepository
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Singleton @Component(modules = [StorageModule::class])
interface AppComponent {
    fun repository(): WalletRepository
    @Component.Factory interface Factory { fun create(@BindsInstance context: Context): AppComponent }
}
@Module object StorageModule {
    @Provides @Singleton fun database(context: Context): NexaPayDatabase = Room.databaseBuilder(context, NexaPayDatabase::class.java, "nexapay.db").fallbackToDestructiveMigrationOnDowngrade().build()
    @Provides fun walletDao(database: NexaPayDatabase): WalletDao = database.walletDao()
    @Provides @Singleton fun repository(dao: WalletDao): WalletRepository = WalletRepository(dao)
}
