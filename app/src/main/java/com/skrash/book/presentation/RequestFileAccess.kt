package com.skrash.book.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class RequestFileAccess {

    companion object {
        fun isStoragePermissionGranted(activity: AppCompatActivity): Boolean {
            return if (Build.VERSION.SDK_INT >= 23) {
                if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1
                    )
                    false
                } else {
                    true
                }
            } else {
                false
            }
        }
    }

}