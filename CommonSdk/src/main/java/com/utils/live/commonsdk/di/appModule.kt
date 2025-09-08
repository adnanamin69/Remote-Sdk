package com.utils.live.commonsdk.di

import com.utils.live.commonsdk.viewmodels_repo.DeviceDiscoveryService
import com.utils.live.commonsdk.viewmodels_repo.PreferencesHelper
import com.utils.live.commonsdk.viewmodels_repo.SmartRepository
import com.utils.live.commonsdk.viewmodels_repo.SmartViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val remoteModule = module {
    single { PreferencesHelper(get()) }
    single { DeviceDiscoveryService(get(), get()) }
    single { SmartRepository(get()) }
    viewModel { SmartViewModel(get(), get(), get()) }
}