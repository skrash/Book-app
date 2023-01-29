package com.skrash.book.presentation.bookInfoActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.usecases.MyList.GetMyBookUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookInfoViewModel @Inject constructor(
    private val getMyBookUseCase: GetMyBookUseCase,
) : ViewModel() {
    private val _bookItem = MutableLiveData<BookItem>()
    val bookItem: LiveData<BookItem>
        get() = _bookItem

    fun getBookItem(bookItemId: Int) {
        viewModelScope.launch {
            val item = getMyBookUseCase.getMyBook(bookItemId)
            _bookItem.value = item
        }
    }
}