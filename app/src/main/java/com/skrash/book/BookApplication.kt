package com.skrash.book

import android.app.Application
import com.skrash.book.di.DaggerApplicationComponent
import com.yandex.mobile.ads.common.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(applicationContext){
        }
    }

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}