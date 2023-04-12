package com.skrash.book.presentation.browseNetworkBookActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.usecases.GetBookItemListUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BrowseNetworkBookViewModel @Inject constructor(
    private val getBookItemListUseCase: GetBookItemListUseCase
) : ViewModel() {

    private val _bookList = MutableLiveData<List<BookItemDto>>()
    val bookList: LiveData<List<BookItemDto>>
        get() = _bookList

    fun getListBooks() {
        CoroutineScope(Dispatchers.IO).launch{
            if (_bookList.value == null) {
                val bookItem = getBookItemListUseCase.getBookItemList()
                _bookList.postValue(bookItem.execute().body())
            }
        }
    }
}