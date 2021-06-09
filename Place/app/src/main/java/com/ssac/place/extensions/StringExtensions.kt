package com.ssac.place.extensions

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

fun String.asClassifyResult(sentence: String, color: Int): Spannable {
    val words = sentence.split(this)
    val prev = words[0]
    val post = words[1]
    val spannable = SpannableString(prev + this + post)
    spannable.setSpan(ForegroundColorSpan(color), prev.length, (prev+this).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannable
}