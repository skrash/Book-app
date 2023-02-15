package com.skrash.book.data.myBook

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class MyBookItemMapper @Inject constructor() {

    fun mapDomainToDbModel(bookItem: BookItem) = MyBookItemDbModel(
        id = bookItem.id,
        title = bookItem.title,
        author = bookItem.author,
        description = bookItem.description,
        rating = bookItem.rating,
        popularity = bookItem.popularity,
        genres = bookItem.genres,
        tags = bookItem.tags,
        path = bookItem.path,
        startOnPage = bookItem.startOnPage,
        fileExtension = bookItem.fileExtension
    )

    fun mapDbModelToDomain(myBookItemDbModel: MyBookItemDbModel) = BookItem(
        id = myBookItemDbModel.id,
        title = myBookItemDbModel.title,
        author = myBookItemDbModel.author,
        description = myBookItemDbModel.description,
        rating = myBookItemDbModel.rating,
        popularity = myBookItemDbModel.popularity,
        genres = myBookItemDbModel.genres,
        tags = myBookItemDbModel.tags,
        path = myBookItemDbModel.path,
        startOnPage = myBookItemDbModel.startOnPage,
        fileExtension = myBookItemDbModel.fileExtension
    )

    fun mapListDbModelToListDomain(list: List<MyBookItemDbModel>) = list.map {
        mapDbModelToDomain(it)
    }
}