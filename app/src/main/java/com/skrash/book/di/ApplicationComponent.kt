package com.skrash.book.di

import android.app.Application
import com.skrash.book.data.BookProvider
import com.skrash.book.presentation.addBookActivity.AddBookItemFragment
import com.skrash.book.presentation.bookInfoActivity.BookInfoFragment
import com.skrash.book.presentation.mainAcitivity.MainActivity
import com.skrash.book.presentation.openBookActivity.fb2Activity.OpenFB2BookActivity
import com.skrash.book.presentation.openBookActivity.pdfActivity.OpenBookActivity
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

    fun inject(contentProvider: BookProvider)

    fun inject(activity: OpenFB2BookActivity)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}