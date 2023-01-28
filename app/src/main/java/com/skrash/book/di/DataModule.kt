package com.skrash.book.di

import com.skrash.book.data.BookItemRepositoryImpl
import com.skrash.book.domain.BookItemRepository
import dagger.Binds
import dagger.Module

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindBookItemRepository(impl: BookItemRepositoryImpl): BookItemRepository

}