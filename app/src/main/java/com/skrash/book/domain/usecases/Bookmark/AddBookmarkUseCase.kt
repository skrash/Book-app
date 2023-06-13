package com.skrash.book.domain.usecases.Bookmark

import com.skrash.book.domain.entities.Bookmark
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {

    suspend fun addBookmark(bookmark: Bookmark){
        bookmarkRepository.addBookmark(bookmark)
    }
}