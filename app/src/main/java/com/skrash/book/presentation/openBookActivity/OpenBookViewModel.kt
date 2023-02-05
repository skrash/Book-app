package com.skrash.book.presentation.openBookActivity

import android.graphics.pdf.PdfRenderer
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

    fun init(bookItemId: Int) {
        viewModelScope.launch {
            val bookItem = getBookItemUseCase.getBookItem(bookItemId)
            if (FormatBook.valueOf(bookItem.fileExtension.uppercase()) == FormatBook.PDF){
                initPDF(bookItem.path)
            }
        }
    }

    private suspend fun initPDF(path: String){
        _pdfRenderer = openBookItemUseCase.openBookItem(path, FormatBook.PDF) as PdfRenderer
        _pageList.value = (0 until _pdfRenderer!!.pageCount).toList()
    }
}