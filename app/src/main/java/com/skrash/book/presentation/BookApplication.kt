package com.skrash.book.presentation

import android.app.Application
import com.skrash.book.di.DaggerApplicationComponent

class BookApplication: Application() {

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}