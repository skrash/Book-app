package com.skrash.book.domain.entities

data class BookItem(
    val title: String,
    val author: String,
    val description: String,
    val rating: Float,
    val popularity: Float,
    val genres: Genres,
    val tags: String,
    val path: String,
    val fileExtension: String,
    val startOnPage: Int,
    val shareAccess: Boolean = false,
    val hash: String,
    val voted: Boolean = false,
    var id: Int = UNDEFINED_ID
){
    companion object {
        const val UNDEFINED_ID = 0
    }
}