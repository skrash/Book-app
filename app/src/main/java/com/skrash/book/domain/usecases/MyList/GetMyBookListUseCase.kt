package com.skrash.book.domain.usecases.MyList

import androidx.lifecycle.LiveData
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetMyBookListUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    suspend fun getMyBookList(): LiveData<List<BookItem>>{
        return bookItemRepository.getMyBookList()
    }
}