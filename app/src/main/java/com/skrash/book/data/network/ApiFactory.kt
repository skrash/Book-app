package com.skrash.book.data.network

import com.skrash.book.data.TorrentSettings
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {

    private const val BASE_URL = "http://${TorrentSettings.DEST_ADDRESS}:${TorrentSettings.DEST_PORT}/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)
}