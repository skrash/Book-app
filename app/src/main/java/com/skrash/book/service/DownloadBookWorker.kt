package com.skrash.book.service

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.skrash.book.data.network.ApiFactory
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
) : CoroutineWorker(context, workerParameters)  {

    private val dataPath = context.getExternalFilesDir(Environment.getDataDirectory().absolutePath)?.absolutePath

    override suspend fun doWork(): Result {
        val bookItemDto = workerParameters.inputData.getString(BookItemDto)
        val title = workerParameters.inputData.getString(TITLE)
        val bookInfoPart = RequestBody.create(MediaType.parse("text/plain"), bookItemDto)
        val response = ApiFactory.apiService.download(bookInfoPart).body()
        if (response != null && title != null) {
            val torrentFile = saveTorrentFile(title, response)
            stopShareService()
            downloadBook(torrentFile)
        }
        return Result.success()
    }

    private fun stopShareService(){
//        context.
    }

    private fun downloadBook(torrentFile: File){
        val address = InetAddress.getByName("192.168.0.100")
        val torrent = Torrent.load(torrentFile)
        val sharedFile = File(dataPath)
        val sharedTorrent = SharedTorrent(torrent, sharedFile)
        val client = Client(address, sharedTorrent)
        client.download()
        Thread{
            while (client.state != Client.ClientState.DONE){
                Thread.sleep(15000)
                Log.d("TEST_WORKER", "client state: ${client.state}")
                Log.d("TEST_WORKER", "client complete: ${client.torrent.completion}")
            }
            // test
            val fileDataPath = File(dataPath)
            val srcFile = File("$dataPath/d_107171776.pdf")
            val destFile = File("/sdcard/Download/d_107171776.pdf")
            destFile.createNewFile()
            copy(srcFile, destFile)
            for (i in fileDataPath.listFiles()){
                Log.d("TEST_WORKER", "data files: ${i.name}}")
            }
        }.start()
    }

    private fun saveTorrentFile(title: String, response: ResponseBody): File{
        val file = File("$dataPath$title.torrent")
        file.writeBytes(response.bytes())
        return file
//        // test
//        val destFile = File("/sdcard/Download/$title.torrent")
//        copy(file, destFile)
    }
//
    fun copy(src: File?, dst: File?) {
        val inStream = FileInputStream(src)
        val outStream = FileOutputStream(dst)
        val inChannel: FileChannel = inStream.channel
        val outChannel: FileChannel = outStream.channel
        inChannel.transferTo(0, inChannel.size(), outChannel)
        inStream.close()
        outStream.close()
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