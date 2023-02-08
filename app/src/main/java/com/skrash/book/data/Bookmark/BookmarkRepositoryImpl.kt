package com.skrash.book.data.Bookmark

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.skrash.book.domain.entities.Bookmark
import com.skrash.book.domain.usecases.Bookmark.BookmarkRepository
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val mapper: BookmarkMapper,
    private val bookmarkDao: BookmarkDao
): BookmarkRepository {
    override suspend fun addBookmark(bookmark: Bookmark) {
        Log.d("TEST10", "bookmark page: ${bookmark.page}, bookmark id: ${bookmark.bookmarkID}, bookmark book id: ${bookmark.bookID}")
        bookmarkDao.addBookmarkItem(mapper.mapBookmarkToBookmarkDbModel(bookmark))
    }

    override suspend fun deleteBookmark(bookmarkID: Int) {
        bookmarkDao.deleteBookmarkItem(bookmarkID)
    }

    override fun getListBookmark(bookmarkID: Int): LiveData<List<Bookmark>> = Transformations.map(
        bookmarkDao.getBookmarkList(bookmarkID)
    ) {
        mapper.mapListBookmarkDbModelToListBookmark(it)
    }
}