package com.skrash.book.service

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.skrash.book.data.network.ApiFactory
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class DownloadBookWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters)  {

    override suspend fun doWork(): Result {
        val bookItemDto = workerParameters.inputData.getString(BookItemDto)
        val title = workerParameters.inputData.getString(TITLE)
        val bookInfoPart = RequestBody.create(MediaType.parse("text/plain"), bookItemDto)
        val response = ApiFactory.apiService.download(bookInfoPart).body()
        Log.d("TEST_WORKER", response.toString())
        response.toString()
        val dataPath = context.getExternalFilesDir(Environment.getDataDirectory().absolutePath)?.absolutePath
        val path = saveFile(response, "$dataPath$title.torrent")
        // test
        val file = File(dataPath)
        for(i in file.listFiles()){
            Log.d("TEST_WORKER", i.name)
        }
        return Result.success()
    }

    fun saveFile(body: ResponseBody?, saveTorrentFilePath: String):String{
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            val fos = FileOutputStream(saveTorrentFilePath)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return saveTorrentFilePath
        }catch (e:Exception){
            Log.e("saveFile",e.toString())
        }
        finally {
            input?.close()
        }
        return ""
    }

    companion object {

        const val WORK_NAME = "DownloadWorker"
        const val BookItemDto = "book_item_dto"
        const val TITLE = "title"

        fun makeRequest(bookItemDto: String, title: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<DownloadBookWorker>()
                .setInputData(workDataOf(BookItemDto to bookItemDto, TITLE to title))
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