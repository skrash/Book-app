package com.skrash.book.domain.usecases.Bookmark

import androidx.lifecycle.LiveData
import com.skrash.book.domain.entities.Bookmark
import javax.inject.Inject

class GetBookmarkListUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
)  {

    fun getBookmarkList(bookmarkID: Int): LiveData<List<Bookmark>>{
        return bookmarkRepository.getListBookmark(bookmarkID)
    }
}