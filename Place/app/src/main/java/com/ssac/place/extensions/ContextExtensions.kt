package com.ssac.place.extensions

import android.content.Context
import android.content.pm.PackageManager
import java.lang.Exception

fun Context.isInstalled(packageName: String): Boolean {
    var result = false
    try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        result = true
    } catch (e: Exception) {
        result = false
    }
    return result
}