package com.skrash.book.data

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import com.skrash.book.FormatBook.FB2
import com.skrash.book.FormatBook.PDF
import com.skrash.book.data.myBook.MyBookItemMapper
import com.skrash.book.data.myBook.MyBookListDao
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import java.io.File
import javax.inject.Inject

class BookItemRepositoryImpl @Inject constructor(
    private val mapper: MyBookItemMapper,
    private val myBookListDao: MyBookListDao
) : BookItemRepository {

    private val bookList = mutableListOf<BookItem>()
    private var autoIncrement = 0
    private var curBook: BookItem? = null

    override suspend fun addBookItem(bookItem: BookItem) {
        if (bookItem.id == BookItem.UNDEFINED_ID) {
            bookItem.id = autoIncrement++
        }
        bookList.add(bookItem)
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

    override suspend fun openBookItem(bookItem: BookItem) {
        curBook = bookItem
    }


    override suspend fun getBookCover(bookItem: BookItem, width: Int, height: Int): Bitmap {
        return when (FormatBook.valueOf(bookItem.fileExtension.uppercase())) {
            FormatBook.PDF -> {
                val file = File(Uri.parse(bookItem.path).path)
                val pdf = PDF(file)
                pdf.getCover(width, height)
            }
            FormatBook.FB2 -> {
                Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_4444)
            }
        }
    }

    override fun getPageBookItem(pageNum: Int, width: Int, height: Int): Bitmap {
        if (curBook != null) {
            return when (FormatBook.valueOf(curBook!!.fileExtension.uppercase())) {
                FormatBook.PDF -> {
                    val file = File(Uri.parse(curBook!!.path).path)
                    val pdf = PDF(file)
                    pdf.openPage(pageNum, width, height)
                }
                FormatBook.FB2 -> {
                    val file = File(curBook!!.path)
                    val fb2 = FB2(file)
                    Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_4444)
                }
            }
        } else {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444)
        }
    }

    override fun getPageCount(bookItem: BookItem): Int {
        if (curBook != null) {
            val formatBook = FormatBook.valueOf(curBook!!.fileExtension.uppercase())
            return when (formatBook) {
                FormatBook.PDF -> {
                    val file = File(Uri.parse(curBook!!.path).path)
                    val pdf = PDF(file)
                    pdf.getPageCount()
                }
                FormatBook.FB2 -> {
                    val file = File(curBook!!.path)
                    val fb2 = FB2(file)
                    fb2.parseFormat()
                    1
                }
            }
        } else {
            return 0
        }
    }
}