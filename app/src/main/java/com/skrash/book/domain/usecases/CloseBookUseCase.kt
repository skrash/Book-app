package com.skrash.book.domain.usecases

import com.skrash.book.domain.BookItemRepository
import javax.inject.Inject

class CloseBookUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
){
    fun closeBook(){
        bookItemRepository.closeBook()
    }
}