package com.skrash.book.data

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.skrash.book.data.myBook.MyBookItemMapper
import com.skrash.book.data.myBook.MyBookListDao
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject

class BookItemRepositoryImpl @Inject constructor(
    private val mapper: MyBookItemMapper,
    private val myBookListDao: MyBookListDao
): BookItemRepository {

    private val bookList = mutableListOf<BookItem>()
    private var autoIncrement = 0
    private var pdfRenderer: PdfRenderer? = null
    private var page: PdfRenderer.Page? = null

    override suspend fun addBookItem(bookItem: BookItem) {
        if (bookItem.id == BookItem.UNDEFINED_ID){
            bookItem.id = autoIncrement++
        }
        bookList.add(bookItem)
        updateGeneralBookList()
    }

    override suspend fun editBookItem(bookItem: BookItem) {
        bookList.remove(getBookItem(bookItem.id))
        addBookItem(bookItem)
    }

    override suspend fun getBookItem(bookItemId: Int): BookItem {
        return mapper.mapDbModelToDomain(myBookListDao.getBookItem(bookItemId))
    }

    override fun getBookItemList(): LiveData<List<BookItem>> {
        return BookListGeneral.bookListGeneral
    }

    override suspend fun openBookItem(path: String, type: FormatBook, width: Int, height: Int): Bitmap {
        val file = File(path)

        when(type){
            FormatBook.PDF -> {
                return openPDF(file, width, height)
            }
        }
    }

    private fun openPDF(file: File, width: Int, height: Int): Bitmap{
        Log.d("TEST", file.path)
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
        page = pdfRenderer!!.openPage(0)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        if (page == null) {
            throw RuntimeException("Incorrect number page is null")
        }
        page!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    override fun closeBook()
    {
        if (page == null){
            throw RuntimeException("")
        }
        page!!.close()
        pdfRenderer!!.close()
    }

    private fun updateGeneralBookList(){
        BookListGeneral.bookListGeneral.postValue(bookList.toList())
    }
}