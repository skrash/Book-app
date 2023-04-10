package com.skrash.book.data.network

import com.skrash.book.data.TorrentSettings
import retrofit2.Retrofit

object ApiFactory {

    private const val BASE_URL = "http://${TorrentSettings.DEST_ADDRESS}:${TorrentSettings.DEST_PORT}/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .build()

    val apiService = retrofit.create(ApiService::class.java)
}