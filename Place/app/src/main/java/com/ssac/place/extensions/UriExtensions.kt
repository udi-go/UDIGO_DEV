package com.ssac.place.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.lang.Exception

fun Uri.getRealPath(context: Context): String? {
    if (path.isNullOrEmpty()) { return null }
//    if (isAbsolute) {
//        return toString()
//    }
    if (path!!.startsWith("/storage")) {
        return path
    }
    val aa = DocumentFile.fromSingleUri(context, this)?.uri
    try {
        val parcelFileDescriptor: ParcelFileDescriptor? =
                context.contentResolver.openFileDescriptor(this, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
    } catch (e: Exception) {

    }
    val id = DocumentsContract.getDocumentId(this).split(":")[1]
    val columns = arrayOf(MediaStore.Images.Media._ID)
    val selection = MediaStore.Files.FileColumns._ID + " = " + id
    val cursor = context.contentResolver.query(this, columns, null, null, null)
    try {
        val columnIndex = cursor?.getColumnIndex(columns[0])
        if (columnIndex != null && cursor.moveToFirst()) {
            val result = cursor.getString(columnIndex)
            return result
        }
    } finally {
        cursor?.close()
    }
    return null
}

fun Uri.getFile(context: Context): File? {
    if (path.isNullOrEmpty()) { return null }
    if (path!!.startsWith("/storage")) { return File(path!!) }
    if (path!!.startsWith("content://")) {
        context.contentResolver.openInputStream(this)
    }
    return File(toString())
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