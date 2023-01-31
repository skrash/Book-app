package com.skrash.book.di

import android.app.Application
import com.skrash.book.data.BookItemRepositoryImpl
import com.skrash.book.data.myBook.MyBookDB
import com.skrash.book.data.myBook.MyBookItemRepositoryImpl
import com.skrash.book.data.myBook.MyBookListDao
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.usecases.MyList.MyBookItemRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindBookItemRepository(impl: BookItemRepositoryImpl): BookItemRepository

    @ApplicationScope
    @Binds
    fun bindMyBookItemRepository(impl: MyBookItemRepositoryImpl): MyBookItemRepository

    companion object {

        @ApplicationScope
        @Provides
        fun provideMyBookListDao(
            application: Application
        ): MyBookListDao {
            return MyBookDB.getInstance(application).myBookListDao()
        }
    }

}