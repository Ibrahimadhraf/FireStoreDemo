package com.ibrahim.firestoredemo

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
const val  REQUEST_Audio_PERMISSION=0
fun hasLocationPermission(context: Context)=

        EasyPermissions.hasPermissions(
            context ,
            Manifest.permission.RECORD_AUDIO ,

        )

