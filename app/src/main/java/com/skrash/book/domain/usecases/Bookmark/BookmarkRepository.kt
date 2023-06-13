package com.skrash.book.domain.usecases.Bookmark

import androidx.lifecycle.LiveData
import com.skrash.book.domain.entities.Bookmark

interface BookmarkRepository {

    suspend fun addBookmark(bookmark: Bookmark)

    suspend fun deleteBookmark(bookmarkID: Int, pageNum: Int)

    fun getListBookmark(bookmarkID: Int): LiveData<List<Bookmark>>
}