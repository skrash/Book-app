package com.skrash.book.di

import android.app.Application
import com.skrash.book.presentation.bookInfoActivity.BookInfoFragment
import com.skrash.book.presentation.mainAcitivity.MainActivity
import com.skrash.book.presentation.addBookActivity.AddBookItemFragment
import com.skrash.book.presentation.openBookActivity.OpenBookActivity
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

    fun inject(activity: OpenBookActivity)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}