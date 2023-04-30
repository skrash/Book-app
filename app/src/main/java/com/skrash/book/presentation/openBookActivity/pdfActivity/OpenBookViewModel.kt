package com.skrash.book.presentation.openBookActivity.pdfActivity

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Bookmark
import com.skrash.book.domain.usecases.Bookmark.AddBookmarkUseCase
import com.skrash.book.domain.usecases.Bookmark.DeleteBookmarkUseCase
import com.skrash.book.domain.usecases.Bookmark.GetBookmarkListUseCase
import com.skrash.book.domain.usecases.MyList.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class OpenBookViewModel @Inject constructor(
    private val getMyBookUseCase: GetMyBookUseCase,
    private val openBookItemUseCase: OpenBookItemUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val getBookmarkListUseCase: GetBookmarkListUseCase,
    private val updateStartOnPageUseCase: UpdateStartOnPageUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val getPageBookItemUseCase: GetPageBookItemUseCase,
    private val getPageCountUseCase: GetPageCountUseCase
) : ViewModel() {

    private var _pageList = MutableLiveData<List<Int>>()
    val pageList: MutableLiveData<List<Int>>
        get() = _pageList


    private var _offsetResidual = 0

    private var _height = 0
    val height
        get() = _height

    private var _page = MutableLiveData("0")
    val page: LiveData<String>
        get() = _page

    private var _bookmarkList = MediatorLiveData<List<Bookmark>>()
    val bookmarkList
        get() = _bookmarkList

    private val _bookItem = MutableLiveData<BookItem>()
    val bookItem: LiveData<BookItem>
        get() = _bookItem

    fun init(bookItemId: Int, height: Int) {
        viewModelScope.launch {
            if (_pageList.value == null){
                _bookItem.value = getMyBookUseCase.getMyBook(bookItemId)
                openBookItemUseCase.openBookItem(_bookItem.value!!)
                val countPages = getPageCountUseCase.getPageCount(_bookItem.value!!)
                _pageList.value = (0 until countPages).toList()
                _height = height
                _offsetResidual = _height / 2
                if (_bookmarkList.value == null) {
                    _bookmarkList.addSource(getBookmarkListUseCase.getBookmarkList(_bookItem.value!!.id)) { bookmark ->
                        _bookmarkList.value = bookmark
                    }
                }
            }
        }
    }

    fun setPage(itemPage: Int) {
        _page.value = itemPage.toString()
    }

    fun jumpTo(page: Int) {
        _page.value = page.toString()
        _offsetResidual = _height / 2
    }


    fun finish(page: Int) {
        viewModelScope.launch {
            if(bookItem.value != null){
                updateStartOnPageUseCase.updateStartOnPage(page, bookItem.value!!.id)
            }
        }
    }

    fun addBookmark(page: Int) {
        viewModelScope.launch {
            val bookmark = Bookmark(bookID = _bookItem.value!!.id, page = page, comment = "")
            addBookmarkUseCase.addBookmark(bookmark)
        }
    }

    fun deleteBookmark(page: Int) {
        viewModelScope.launch {
            if (_bookItem.value != null) {
                deleteBookmarkUseCase.deleteBookmark(_bookItem.value!!.id, page)
            }
        }
    }

    fun getPage(pageNum: Int, width: Int, height: Int): Bitmap{
        return if (_bookItem.value != null){
            getPageBookItemUseCase.getPageBookItem(pageNum, width, height)
        } else {
            Bitmap.createBitmap(
                width, height,
                Bitmap.Config.ARGB_4444
            )
        }
    }
}