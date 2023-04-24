package com.skrash.book.data.network.model

import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres

class BookItemDtoMapper {

    companion object{

        fun bookItemDtoToBookItem(bookItemDto: BookItemDto) = BookItem(
            title = bookItemDto.title ?: "",
            author = bookItemDto.author ?: "",
            description = bookItemDto.description ?: "",
            rating = bookItemDto.rating?.toFloat() ?: 0f,
            popularity = bookItemDto.popularity?.toFloat() ?: 0f,
            genres = Genres.valueOf(bookItemDto.genres ?: Genres.Other.name),
            tags = bookItemDto.tags ?: "",
            path = "",
            fileExtension = bookItemDto.fileExtension ?: "",
            startOnPage = 0,
            shareAccess = false,
            id = -1
        )

        fun bookItemDtoListToBookItemList(list: List<BookItemDto>) = list.map {
            bookItemDtoToBookItem(it)
        }

        fun bookItemToBookItemDto(bookItem: BookItem) = BookItemDto(
            author = bookItem.author,
            description = bookItem.description,
            genres = bookItem.genres.name,
            popularity = bookItem.popularity.toDouble(),
            rating = bookItem.rating.toDouble(),
            tags = bookItem.tags,
            title = bookItem.title,
            fileExtension = bookItem.fileExtension
        )
    }
}