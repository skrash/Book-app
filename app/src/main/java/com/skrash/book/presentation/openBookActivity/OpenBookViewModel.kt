package com.skrash.book.presentation.openBookActivity

import android.graphics.pdf.PdfRenderer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.usecases.GetBookItemUseCase
import com.skrash.book.domain.usecases.OpenBookItemUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class OpenBookViewModel @Inject constructor(
    private val getBookItemUseCase: GetBookItemUseCase,
    private val openBookItemUseCase: OpenBookItemUseCase
) : ViewModel() {

    private var _pageList = MutableLiveData<List<Int>>()
    val pageList: MutableLiveData<List<Int>>
        get() = _pageList

    private var _pdfRenderer: PdfRenderer? = null
    val pdfRenderer: PdfRenderer?
        get() = _pdfRenderer

    private var _offsetResidual = 0

    private var _height = 0
    private var _page = MutableLiveData("0")
    val page: MutableLiveData<String>
        get() = _page

    fun init(bookItemId: Int, height: Int) {
        viewModelScope.launch {
            val bookItem = getBookItemUseCase.getBookItem(bookItemId)
            if (FormatBook.valueOf(bookItem.fileExtension.uppercase()) == FormatBook.PDF) {
                initPDF(bookItem.path)
            }
            _height = height
            _offsetResidual = _height / 2
            Log.d("TEST5", _height.toString())
        }
    }

    fun scrolling(offset: Int) {
        _page.value = (_page.value!!.toInt() + ( offset / _height)).toString()
        Log.d("TEST5", ((_offsetResidual + offset) / _height > 1).toString())
        Log.d("TEST5", ((_offsetResidual + offset) / _height).toString())
        if ((_offsetResidual + offset) / _height >= 1)
        {
            _page.value = (_page.value!!.toInt()+ (_offsetResidual + offset) / _height).toString()
            _offsetResidual += offset % _height
            _offsetResidual = 0
            Log.d("TEST5", "RESETED")
        } else {
            _offsetResidual += offset % _height
        }
        Log.d("TEST5", "offset $offset, _offsetResidual $_offsetResidual")
    }

    private suspend fun initPDF(path: String) {
        _pdfRenderer = openBookItemUseCase.openBookItem(path, FormatBook.PDF) as PdfRenderer
        _pageList.value = (0 until _pdfRenderer!!.pageCount).toList()
    }
}