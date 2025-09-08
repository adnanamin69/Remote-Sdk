package com.sdk.remote.remoterepo

import android.app.Application
import android.os.Build
import android.util.Log
import com.sdk.remote.androidremote.RemoteNDKHelper
import com.utils.live.commonsdk.di.remoteModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class RemoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RemoteNDKHelper.initialize(this)
        startKoin {
            androidContext(this@RemoteApp)
            modules(appModule, remoteModule)
            androidLogger()
        }

        Log.i("Command", "onCreate: ${Build.VERSION.SDK_INT}")

        //   YourNDKHelper.loadLib()
    }
}


val appModule = module {
    //  viewModel { SmartViewModel(get()) }
}
