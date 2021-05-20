package com.ssac.place.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.IOException

fun Uri.getRealPath(context: Context): String? {
    if (path.isNullOrEmpty()) { return null }
    if (isAbsolute) {
        return toString()
    }
    if (path!!.startsWith("/storage") || path!!.startsWith("content://")) {
        return path
    }
    val id = DocumentsContract.getDocumentId(this).split(":")[1]
    val columns = arrayOf(MediaStore.Files.FileColumns.DATA)
    val selection = MediaStore.Files.FileColumns._ID + " = " + id
    val cursor = context.contentResolver.query(this, columns, selection, null, null)
    try {
        val columnIndex = cursor?.getColumnIndex(columns[0])
        if (columnIndex != null && cursor.moveToFirst()) {
            return cursor.getString(columnIndex)
        }
    } finally {
        cursor?.close()
    }
    return null
}

fun Uri.loadBitmap(context: Context): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT > 27) {
            val source: ImageDecoder.Source = ImageDecoder.createSource(context.contentResolver, this)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}