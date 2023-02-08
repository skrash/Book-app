package com.skrash.book.data.myBook

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.usecases.MyList.MyBookItemRepository
import javax.inject.Inject

class MyBookItemRepositoryImpl @Inject constructor(
    private val myBookListDao: MyBookListDao,
    private val mapper: MyBookItemMapper
): MyBookItemRepository {

    override suspend fun addToMyBookList(bookItem: BookItem) {
        myBookListDao.addMyBookItem(mapper.mapDomainToDbModel(bookItem))
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
        myBookListDao.deleteMyBookItem(bookItem.id)
    }

    override suspend fun editMyBookItem(bookItem: BookItem) {
        myBookListDao.addMyBookItem(mapper.mapDomainToDbModel(bookItem))
    }

    override suspend fun updateStartOnPage(pageNum: Int, bookID: Int) {
        myBookListDao.updatePage(pageNum, bookID)
    }
}