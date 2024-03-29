package com.skrash.book.di

import androidx.lifecycle.ViewModel
import com.skrash.book.presentation.addBookActivity.AddBookItemViewModel
import com.skrash.book.presentation.bookInfoActivity.BookInfoViewModel
import com.skrash.book.presentation.mainAcitivity.MainActivityViewModel
import com.skrash.book.presentation.openBookActivity.fb2Activity.OpenFB2BookViewModel
import com.skrash.book.presentation.openBookActivity.pdfActivity.OpenBookViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun bindMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookInfoViewModel::class)
    fun bindBookInfoViewModel(viewModel: BookInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddBookItemViewModel::class)
    fun bindAddBookItemViewModel(viewModel: AddBookItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OpenBookViewModel::class)
    fun bindOpenBookViewModel(viewModel: OpenBookViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OpenFB2BookViewModel::class)
    fun bindOpenFB2BookViewModel(viewModel: OpenFB2BookViewModel): ViewModel
}