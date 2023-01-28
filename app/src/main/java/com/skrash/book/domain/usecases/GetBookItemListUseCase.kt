package com.skrash.book.domain.usecases

import androidx.lifecycle.LiveData
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetBookItemListUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    fun getBookItemList(): LiveData<List<BookItem>>{
        return bookItemRepository.getBookItemList()
    }
}