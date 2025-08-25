package com.adnan.remote.remoterepo

import android.app.Application
import com.adnan.remote.androidremote.YourNDKHelper
import com.adnan.remote.remoterepo.viewmodels.SmartViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class RemoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RemoteApp)
            modules(appModule)
            androidLogger()
        }
        //   YourNDKHelper.loadLib()
    }
}


val appModule = module {
    viewModel { SmartViewModel(get()) }
}
