package com.ssac.place.extensions

import android.content.res.Resources

fun Int.dp(): Int {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}