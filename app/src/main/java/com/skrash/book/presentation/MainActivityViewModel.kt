package com.skrash.book.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.AddBookItemUseCase
import com.skrash.book.domain.usecases.GetBookItemListUseCase
import com.skrash.book.domain.usecases.MyList.AddToMyBookListUseCase
import com.skrash.book.domain.usecases.MyList.DeleteBookItemFromMyListUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookListUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivityViewModel @Inject constructor(
    private val getMyBookListUseCase: GetMyBookListUseCase,
    private val addToMyBookListUseCase: AddToMyBookListUseCase,
    private val deleteBookItemFromMyListUseCase: DeleteBookItemFromMyListUseCase
) : ViewModel() {

    val bookList = getMyBookListUseCase.getMyBookList()

    fun deleteShopItem(bookItem: BookItem){
        viewModelScope.launch {
            deleteBookItemFromMyListUseCase.deleteBookItemFromMyList(bookItem)
        }
    }

    init {
        viewModelScope.launch {
            for( i in 0 until 10) {
                addToMyBookListUseCase.addToMyBookList(
                    BookItem(
                        i.toString(),
                        i.toString(),
                        i.toString(),
                        Random.nextInt(1, 5) + Random.nextFloat(),
                        Random.nextInt(1, 5) + Random.nextFloat(),
                        Genres.nan,
                        "${this.toString()}1,${this.toString()}2,${this.toString()}3,${this.toString()}4,${this.toString()}5,${this.toString()}6,${this.toString()}7",
                        "img",
                        ".pdf",
                        i
                    )
                )
            }
        }
    }
}