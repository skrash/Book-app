package com.skrash.book.domain.entities

data class BookItem(
    val title: String,
    val author: String,
    val description: String,
    val rating: Float,
    val popularity: Float,
    val genres: Genres,
    val tags: String,
    val cover: String,
    val fileExtension: String,
    var id: Int = UNDEFINED_ID
){
    companion object {
        const val UNDEFINED_ID = -1
    }
}