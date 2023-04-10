package com.skrash.book.data.network

import com.skrash.book.domain.entities.BookItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("create")
    @Multipart
    suspend fun publishTorrent(@Part tfile: MultipartBody.Part, @Part("data") bookItem: RequestBody)
}