package com.skrash.book.data.myBook

import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.skrash.book.FormatBook.FB2
import com.skrash.book.FormatBook.PDF
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.usecases.MyList.MyBookItemRepository
import java.io.File
import javax.inject.Inject

class MyBookItemRepositoryImpl @Inject constructor(
    private val myBookListDao: MyBookListDao,
    private val mapper: MyBookItemMapper
): MyBookItemRepository {

    private var curBook: BookItem? = null
    override suspend fun addToMyBookList(bookItem: BookItem): Long {
        return myBookListDao.addMyBookItem(mapper.mapDomainToDbModel(bookItem))
    }

    override fun getMyBookList(): LiveData<List<BookItem>> = Transformations.map(
        myBookListDao.getMyBookList()
    ){
        mapper.mapListDbModelToListDomain(it)
    }

    override suspend fun getMyBook(bookItemId: Int): BookItem {
        return mapper.mapDbModelToDomain(myBookListDao.getBookItem(bookItemId))
    }

    override suspend fun deleteBookItemFromMyList(bookItem: BookItem) {
        val file = File(bookItem.path)
        if (!file.delete()){
            throw RuntimeException("could not delete file")
        }
        myBookListDao.deleteMyBookItem(bookItem.path)
    }

    override suspend fun editMyBookItem(bookItem: BookItem) {
        myBookListDao.addMyBookItem(mapper.mapDomainToDbModel(bookItem))
    }

    override suspend fun updateStartOnPage(pageNum: Int, bookID: Int) {
        myBookListDao.updatePage(pageNum, bookID)
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