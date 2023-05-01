package com.skrash.book.domain.entities

data class FB2MetaInfo(
    val author: String,
    val fb_id: String,
    val publisher: String,
    val year: Int,
    val title: String,
    val coverPageResName: String,
    val tag: String
){
    companion object {
        const val UNDEFINED_ID = -1
    }
}