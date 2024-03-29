package com.skrash.book.data.network

import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.data.network.model.RequestUpdateDto
import com.skrash.book.data.network.model.UpdateItemDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("create")
    @Multipart
    suspend fun publishTorrent(@Part tfile: MultipartBody.Part, @Part("data") bookItem: RequestBody)

    @GET("announce")
    suspend fun announce(
        @Query("info_hash") hash: String,
        @Query("peer_id") peer_id: String,
        @Query("uploaded") uploaded: Int,
        @Query("downloaded") downloaded: Int,
        @Query("left") left: Int,
        @Query("port") port: Int
    ): ResponseBody

    @GET("list_books")
    fun listBook(@Query("userID") userID: String): Call<List<BookItemDto>>

    @GET("my_ip")
    suspend fun getIp(): ResponseBody

    @POST("download")
    suspend fun download(
        @Body bookItem: RequestBody
    ): Response<ResponseBody>

    @GET("vote")
    suspend fun vote(
        @Query("hash") hash: String,
        @Query("userID") userID: String,
        @Query("votePoint") votePoint: Int
    ): ResponseBody

    @POST("get_update")
    suspend fun getUpdate(
        @Body requestUpdate: RequestUpdateDto
    ): List<UpdateItemDto>
}