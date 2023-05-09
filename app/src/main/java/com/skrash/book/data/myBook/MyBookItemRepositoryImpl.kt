package com.skrash.book.data.myBook

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.skrash.book.FormatBook.FB2
import com.skrash.book.FormatBook.PDF
import com.skrash.book.data.TorrentSettings
import com.skrash.book.data.network.ApiFactory
import com.skrash.book.data.network.model.RequestUpdateDto
import com.skrash.book.data.network.model.UpdateItemDto
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.usecases.MyList.MyBookItemRepository
import com.skrash.book.torrent.client.common.TorrentCreator
import com.skrash.book.torrent.client.common.TorrentUtils
import retrofit2.Call
import java.io.File
import java.net.ConnectException
import java.net.URI
import javax.inject.Inject

class MyBookItemRepositoryImpl @Inject constructor(
    private val myBookListDao: MyBookListDao,
    private val mapper: MyBookItemMapper
) : MyBookItemRepository {

    private var curBook: BookItem? = null
    override suspend fun addToMyBookList(bookItem: BookItem): Long {
        return myBookListDao.addMyBookItem(mapper.mapDomainToDbModel(bookItem))
    }

    override fun getMyBookList(): LiveData<List<BookItem>> = Transformations.map(
        myBookListDao.getMyBookList()
    ) {
        mapper.mapListDbModelToListDomain(it)
    }

    override suspend fun getMyBook(bookItemId: Int): BookItem {
        return mapper.mapDbModelToDomain(myBookListDao.getBookItem(bookItemId))
    }

    override suspend fun deleteBookItemFromMyList(bookItem: BookItem) {
        val file = File(bookItem.path)
        if (!file.delete()) {
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
                val file = File(Uri.parse(bookItem.path).path)
                val fb2 = FB2(file)
                if (fb2.fb2!!.binaries.isNotEmpty()) {
                    val imageBytes = Base64.decode(
                        fb2.fb2!!.binaries.iterator().next().value.binary,
                        Base64.DEFAULT
                    )
                    val decodedImage =
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    decodedImage
                } else {
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444)
                }
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
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444)
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
                    0
                }
            }
        } else {
            return 0
        }
    }

    override fun getHash(path: String): String {
        return TorrentCreator.create(File(path), URI.create("http://${TorrentSettings.DEST_ADDRESS}:${TorrentSettings.DEST_PORT}/announce"), "").hexInfoHash.lowercase()
    }

    override suspend fun getAllMyBookHashes(): List<String> {
        return myBookListDao.getAllHashes()
    }

    override suspend fun getMyBookItemByHash(hash: String): BookItem {
        return myBookListDao.getBookItemByHash(hash)
    }

    override suspend fun getUpdate(
        userID: String,
    ){
        val listHashes = myBookListDao.getAllHashes()
        if (listHashes.isNotEmpty()){
            val requestUpdateDto = RequestUpdateDto(
                userID,
                listHashes
            )
            try {
                val updateItemDtoList = ApiFactory.apiService.getUpdate(requestUpdateDto)
                if (updateItemDtoList != null) {
                    for (updateItem in updateItemDtoList){
                        myBookListDao.updateBDInfo(updateItem.rating.toFloat(), updateItem.popularity.toFloat(), updateItem.voted, updateItem.hash)
                    }
                }
            }catch (e: ConnectException){
                e.printStackTrace()
            }
        }
    }
}