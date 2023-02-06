package com.skrash.book.domain.usecases.Bookmark

import javax.inject.Inject

class DeleteBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
)  {

    suspend fun deleteBookmark(bookmarkID: Int){
        bookmarkRepository.deleteBookmark(bookmarkID)
    }
}