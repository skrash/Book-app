package com.skrash.book.torrent

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.google.gson.Gson
import com.skrash.book.data.network.ApiFactory
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.torrent.client.SimpleClient
import com.skrash.book.torrent.client.common.*
import com.skrash.book.torrent.client.peer.SharingPeer
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.math.roundToInt


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
            downloadBook(torrentFile, bookJson)
        } else {
            Log.d("TEST_WORKER", "response null")
        }
        // TODO: Костыль. Ожидание добавлено чтобы setProgress успел выполниться раньше, чем воркер завершит работу
        delay(3000)
        return Result.success()
    }

    private suspend fun downloadBook(torrentFile: File, bookItemDto: BookItemDto) {
        setProgress(workDataOf(TAG_PROGRESS to 0))
        val client = SimpleClient { _, peerInformation ->
            if ((peerInformation as SharingPeer).torrent.completedPieces.cardinality() != 0) {
                CoroutineScope(Dispatchers.Unconfined).launch {
                    if (peerInformation.torrent.completedPieces.cardinality() + 1 == peerInformation.torrent.pieceCount) {
                        setProgress(workDataOf(TAG_PROGRESS to 100))
                    } else {
                        setProgress(
                            workDataOf(
                                TAG_PROGRESS to ((peerInformation.torrent.completedPieces.cardinality()
                                    .toFloat() / peerInformation.torrent.pieceCount.toFloat()) * 100).roundToInt()
                            )
                        )
                    }
                }
            }
        }
        val metadata = TorrentParser().parseFromFile(torrentFile)
        val ipResponse = ApiFactory.apiService.getIp()
        val address = InetAddress.getByName(ipResponse.string())
        try {
            client.downloadTorrent(
                torrentFile.path,
                dataPath,
                address
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        client.stop()
        addToDbBook((metadata as TorrentMetadataImpl).myName, bookItemDto)
    }

    private fun saveTorrentFile(title: String, response: ResponseBody): File {
        Log.d("TEST_WORKER", "title: $title")
        val file = File("$dataPath$title.torrent")
        file.writeBytes(response.bytes())
        return file
    }

    private suspend fun addToDbBook(downloadedFileName: String, bookItemDto: BookItemDto) {
        val contentValues = ContentValues()
        contentValues.put("title", bookItemDto.title)
        contentValues.put("author", bookItemDto.author)
        contentValues.put("description", bookItemDto.description)
        contentValues.put("genres", bookItemDto.genres)
        contentValues.put("popularity", bookItemDto.popularity)
        contentValues.put("rating", bookItemDto.rating)
        contentValues.put("fileExtension", bookItemDto.fileExtension)
        contentValues.put("tags", bookItemDto.tags)
        contentValues.put("path", "$dataPath/$downloadedFileName")
        contentValues.put("hash", bookItemDto.hash)
        val uri = context.contentResolver.insert(
            Uri.parse("content://com.skrash.book/create"),
            contentValues
        )?.path ?: throw RuntimeException("Could not get uri")
        val id = uri.split("/").last().toInt()
        runBlocking {
            setProgress(workDataOf(TAG_CREATED_BOOK_ID to id))
        }
    }

    companion object {

        const val WORK_NAME = "DownloadWorker"
        const val BookItemDto = "book_item_dto"
        const val TAG_PROGRESS = "progress"
        const val TAG_CREATED_BOOK_ID = "created_book_id"

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