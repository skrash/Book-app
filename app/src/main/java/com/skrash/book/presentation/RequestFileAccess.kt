package com.skrash.book.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class RequestFileAccess {

    companion object {

        const val REQUEST_CODE = 100

        fun requestFileAccessPermission(activity: AppCompatActivity, permissionGranted: ()->Unit, permissionNotGranted: ()-> Unit) {
            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
                permissionNotGranted()
            } else {
                permissionGranted()
            }
        }
    }

}