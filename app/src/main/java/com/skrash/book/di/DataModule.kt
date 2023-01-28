package com.skrash.book.di

import com.skrash.book.data.BookItemRepositoryImpl
import com.skrash.book.data.MyBookItemRepositoryImpl
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.usecases.MyList.MyBookItemRepository
import dagger.Binds
import dagger.Module

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindBookItemRepository(impl: BookItemRepositoryImpl): BookItemRepository

    @ApplicationScope
    @Binds
    fun bindMyBookItemRepository(impl: MyBookItemRepositoryImpl): MyBookItemRepository

}