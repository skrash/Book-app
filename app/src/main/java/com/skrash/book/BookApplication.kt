package com.skrash.book

import android.app.Application
import com.skrash.book.di.DaggerApplicationComponent
import com.yandex.mobile.ads.common.MobileAds

class BookApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this){

        }
    }

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}