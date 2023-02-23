package com.skrash.book.domain.usecases

import android.graphics.Bitmap
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetBookCoverUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    suspend fun getBookCover(bookItem: BookItem, width: Int, height: Int): Bitmap {
        return bookItemRepository.getBookCover(bookItem, width, height)
    }
}