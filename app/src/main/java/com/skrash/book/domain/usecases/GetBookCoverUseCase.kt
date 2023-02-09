package com.skrash.book.domain.usecases

import android.graphics.Bitmap
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.FormatBook
import javax.inject.Inject

class GetBookCoverUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    suspend fun getBookCover(bookFormat: FormatBook, path: String, width: Int, height: Int): Bitmap {
        return bookItemRepository.getBookCover(bookFormat, path, width, height)
    }
}