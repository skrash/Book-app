package com.skrash.book.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.skrash.book.R
import com.turn.ttorrent.client.Client
import com.turn.ttorrent.client.SharedTorrent
import com.turn.ttorrent.common.Torrent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.FileChannel


class TorrentService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Title")
        .setContentText("Text")
        .setSmallIcon(R.drawable.ic_launcher_background)
        .build()


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
//            copyToData()
            try {
                Log.d("TEST_WORKER", "started")
                val address = InetAddress.getLocalHost() // InetSocketAddress("127.0.0.1",39994)
                    val dataPath = getExternalFilesDir(Environment.getDataDirectory().absolutePath)
                    ?.absolutePath
                val torrentFile = File(dataPath + "/d_107171776.torrent")
                val torrent = Torrent.load(torrentFile)

                Log.d("TEST_WORKER", "ffile exist? ${File(dataPath + "/d_107171776.pdf").exists()}")

                val sharedFile = File(dataPath)
                for (i in sharedFile.listFiles()) {
                    Log.d("TEST_WORKER", "files in data: ${i.name}")
                }
                val sharedTorrent = SharedTorrent(torrent, sharedFile)
                val client = Client(address, sharedTorrent)
                client.share()
//                client.share()
                for (i in 0 .. 10){
                    Thread.sleep(3000)
                    Log.d("TEST_WORKER", "completion: ${sharedTorrent.completion}")
                    Log.d("TEST_WORKER", "client state: ${client.state}")
                }
            } catch (e: Exception) {
                Log.d("TEST_WORKER", "torrent client: ${e.localizedMessage}")
            }
        }
        return START_STICKY
    }

    private fun copyToData(): File {
        val srcFile = File("/sdcard/Download/d_107171776.pdf")
        val dataPath =
            getExternalFilesDir(Environment.getDataDirectory().absolutePath)?.absolutePath
        val destFile = File(dataPath + "/d_107171776.pdf")
        srcFile.copyTo(destFile, true)
//        destFile.createNewFile()
//        val inStream = FileInputStream(srcFile)
//        val outStream = FileOutputStream(destFile)
//        val inChannel: FileChannel = inStream.channel
//        val outChannel: FileChannel = outStream.channel
//        inChannel.transferTo(0, inChannel.size(), outChannel)
//        inStream.close()
//        outStream.close()
        return destFile
    }

    companion object {

        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "channel_name"
        private const val NOTIFICATION_ID = 1

        fun newIntent(context: Context): Intent {
            return Intent(context, TorrentService::class.java)
        }
    }
}