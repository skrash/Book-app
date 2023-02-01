package com.skrash.book.di

import androidx.lifecycle.ViewModel
import com.skrash.book.presentation.bookInfoActivity.BookInfoViewModel
import com.skrash.book.presentation.MainActivityViewModel
import com.skrash.book.presentation.addBookActivity.AddBookItemViewModel
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
}