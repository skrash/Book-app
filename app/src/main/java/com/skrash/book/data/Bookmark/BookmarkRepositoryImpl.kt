package com.skrash.book.data.Bookmark

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
        bookmarkDao.addBookmarkItem(mapper.mapBookmarkToBookmarkDbModel(bookmark))
    }

    override suspend fun deleteBookmark(bookmarkID: Int) {
        bookmarkDao.deleteBookmarkItem(bookmarkID)
    }

    override fun getListBookmark(): LiveData<List<Bookmark>> = Transformations.map(
        bookmarkDao.getBookmarkList()
    ) {
        mapper.mapListBookmarkDbModelToListBookmark(it)
    }
}