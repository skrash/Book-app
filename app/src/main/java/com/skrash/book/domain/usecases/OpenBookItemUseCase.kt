package com.skrash.book.domain.usecases

import android.graphics.Bitmap
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import javax.inject.Inject

class OpenBookItemUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    suspend fun openBookItem(path: String, type: FormatBook, width: Int, height: Int): Bitmap{
        return bookItemRepository.openBookItem(path, type, width, height)
    }
}