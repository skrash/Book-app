package com.skrash.book.presentation.openBookActivity.fb2Activity

import android.util.Log
import androidx.lifecycle.*
import com.skrash.book.FormatBook.FB2
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Bookmark
import com.skrash.book.domain.usecases.Bookmark.AddBookmarkUseCase
import com.skrash.book.domain.usecases.Bookmark.DeleteBookmarkUseCase
import com.skrash.book.domain.usecases.Bookmark.GetBookmarkListUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookUseCase
import com.skrash.book.domain.usecases.MyList.UpdateStartOnPageUseCase
import com.skrash.book.presentation.openBookActivity.BookMethods
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

class OpenFB2BookViewModel @Inject constructor(
    private val getMyBookUseCase: GetMyBookUseCase,
    private val updateStartOnPageUseCase: UpdateStartOnPageUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val getBookmarkListUseCase: GetBookmarkListUseCase,
) : ViewModel(), BookMethods {

    private val _bookItem = MutableLiveData<BookItem>()
    val bookItem: LiveData<BookItem>
        get() = _bookItem

    private var _fb2 = MutableLiveData<FB2>()
    val fb2: LiveData<FB2>
        get() = _fb2

    private var _offset = MutableLiveData("0")
    val offset: LiveData<String>
        get() = _offset

    private var _bookmarkList = MediatorLiveData<List<Bookmark>>()
    val bookmarkList
        get() = _bookmarkList

    fun init(bookItemId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (_bookItem.value == null) {
                val item = getMyBookUseCase.getMyBook(bookItemId)
                _bookItem.postValue(item)
                val file = File(item.path)
                _fb2.postValue(FB2(file))
                withContext(Dispatchers.Main) {
                    if (_bookmarkList.value == null) {
                        _bookmarkList.addSource(getBookmarkListUseCase.getBookmarkList(item.id)) { bookmark ->
                            _bookmarkList.value = bookmark
                        }
                    }
                }
            }
        }
    }

    override fun setPage(itemPage: Int) {
        _offset.value = itemPage.toString()
    }

    override fun jumpTo(page: Int) {
        _offset.value = page.toString()
    }

    override fun finish(page: Int) {
        viewModelScope.launch {
            if (_offset.value != null) {
                updateStartOnPageUseCase.updateStartOnPage(
                    _offset.value?.toInt() ?: 0,
                    _bookItem.value!!.id
                )
            }
        }
    }

    override fun addBookmark(page: Int) {
        viewModelScope.launch {
            val bookmark = Bookmark(bookID = _bookItem.value!!.id, page = page, comment = "")
            addBookmarkUseCase.addBookmark(bookmark)
        }
    }

    override fun deleteBookmark(page: Int) {
        viewModelScope.launch {
            if (_bookItem.value != null) {
                deleteBookmarkUseCase.deleteBookmark(_bookItem.value!!.id, page)
            }
        }
    }

}