package com.skrash.book.di

import android.app.Application
import com.skrash.book.data.BookItemRepositoryImpl
import com.skrash.book.data.Bookmark.BookmarkDao
import com.skrash.book.data.Bookmark.BookmarkRepositoryImpl
import com.skrash.book.data.MyBookDB
import com.skrash.book.data.myBook.MyBookItemRepositoryImpl
import com.skrash.book.data.myBook.MyBookListDao
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.usecases.Bookmark.BookmarkRepository
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

    @ApplicationScope
    @Binds
    fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    companion object {

        @ApplicationScope
        @Provides
        fun provideMyBookListDao(
            application: Application
        ): MyBookListDao {
            return MyBookDB.getInstance(application).myBookListDao()
        }

        @ApplicationScope
        @Provides
        fun provideBookmarkDao(
            application: Application
        ): BookmarkDao {
            return MyBookDB.getInstance(application).bookmarkDao()
        }
    }

}