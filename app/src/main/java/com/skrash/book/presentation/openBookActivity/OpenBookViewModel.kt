package com.skrash.book.presentation.openBookActivity

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.usecases.CloseBookUseCase
import com.skrash.book.domain.usecases.GetBookItemUseCase
import com.skrash.book.domain.usecases.OpenBookItemUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class OpenBookViewModel @Inject constructor(
    private val getBookItemUseCase: GetBookItemUseCase,
    private val openBookItemUseCase: OpenBookItemUseCase,
    private val closeBookUseCase: CloseBookUseCase
) : ViewModel() {

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap>
        get() = _bitmap
    private var _bookPath: String? = null
    private var _bookItemType: FormatBook? = null

    fun init(bookItemId: Int, width: Int, height: Int) {
        viewModelScope.launch {
            val bookItem = getBookItemUseCase.getBookItem(bookItemId)
            _bookPath = bookItem.path
            _bookItemType = FormatBook.valueOf(bookItem.fileExtension.uppercase())
            if (width == 0 || height == 0) {
                throw RuntimeException("Width or/and height is 0. values: $width, $height")
            }
            getBitmap(width, height)
        }
    }


    private suspend fun getBitmap(width: Int, height: Int) {
        if (_bookPath == null || _bookItemType == null || width == null || height == null) {
            throw RuntimeException("Init variables is null! values: $_bookPath , $_bookItemType, $width, $height")
        } else {
            Log.d("TEST", _bookPath.toString())
            _bitmap.value =
                openBookItemUseCase.openBookItem(_bookPath!!, _bookItemType!!, width!!, height!!)
        }
    }

    fun closeRender() {
        closeBookUseCase.closeBook()
    }
}