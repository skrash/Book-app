package com.skrash.book.service

import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.google.gson.Gson
import com.skrash.book.data.network.ApiFactory
import com.skrash.book.data.network.model.BookItemDto
import com.turn.ttorrent.client.Client
import com.turn.ttorrent.client.SharedTorrent
import com.turn.ttorrent.common.Torrent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.nio.channels.FileChannel


class DownloadBookWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val dataPath =
        context.getExternalFilesDir(Environment.getDataDirectory().absolutePath)?.absolutePath

    override suspend fun doWork(): Result {
        Log.d("TEST_WORKER", "started")
        val bookItemDto = workerParameters.inputData.getString(BookItemDto)
        val gson = Gson()
        val bookJson =
            gson.fromJson(bookItemDto, com.skrash.book.data.network.model.BookItemDto::class.java)
        val bookInfoPart = RequestBody.create(MediaType.parse("text/plain"), bookItemDto)
        val response = ApiFactory.apiService.download(bookInfoPart).body()
        if (response != null) {
            val torrentFile = saveTorrentFile(
                bookJson.title
                    ?: throw java.lang.RuntimeException("download worker request wrong data. missing title book"),
                response
            )
            stopShareService()
            downloadBook(torrentFile, bookJson)
        } else {
            Log.d("TEST_WORKER", "response null")
        }
        return Result.success()
    }

    private fun stopShareService() {
//        context.
    }

    private fun downloadBook(torrentFile: File, bookItemDto: BookItemDto) {
        val address = InetAddress.getByName("192.168.0.100")
        val torrent = Torrent.load(torrentFile)
        val sharedFile = File(dataPath)
        val sharedTorrent = SharedTorrent(torrent, sharedFile)
        val client = Client(address, sharedTorrent)
        try {
            client.download()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        while (!client.torrent.isComplete){
            Thread.sleep(3000)
        }
        client.stop(true)
        Log.d("TEST_WORKER", client.state.name)
        addToDbBook(bookItemDto)
    }

    private fun saveTorrentFile(title: String, response: ResponseBody): File {
        Log.d("TEST_WORKER", "title: $title")
        val file = File("$dataPath$title.torrent")
        file.writeBytes(response.bytes())
        return file
    }

    private fun addToDbBook(bookItemDto: BookItemDto) {
        val contentValues = ContentValues()
        contentValues.put("title", bookItemDto.title)
        contentValues.put("author", bookItemDto.author)
        contentValues.put("description", bookItemDto.description)
        contentValues.put("genres", bookItemDto.genres)
        contentValues.put("popularity", bookItemDto.popularity)
        contentValues.put("rating", bookItemDto.rating)
        contentValues.put("fileExtension", bookItemDto.fileExtension)
        contentValues.put("tags", bookItemDto.tags)
        contentValues.put("path", "$dataPath/${bookItemDto.title}.${bookItemDto.fileExtension}")
        context.contentResolver.insert(Uri.parse("content://com.skrash.book/create"), contentValues)
    }

    companion object {

        const val WORK_NAME = "DownloadWorker"
        const val BookItemDto = "book_item_dto"

        fun makeRequest(bookItemDto: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<DownloadBookWorker>()
                .setInputData(workDataOf(BookItemDto to bookItemDto))
                .setConstraints(makeConstraints())
                .build()
        }

        private fun makeConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        }

    }

}