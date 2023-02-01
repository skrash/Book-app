package com.skrash.book.di

import android.app.Application
import com.skrash.book.presentation.BookInfoFragment
import com.skrash.book.presentation.MainActivity
import com.skrash.book.presentation.addBookActivity.AddBookItemFragment
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    fun inject(fragment: BookInfoFragment)

    fun inject(fragment: AddBookItemFragment)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}