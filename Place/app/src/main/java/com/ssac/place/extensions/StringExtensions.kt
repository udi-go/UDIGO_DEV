package com.ssac.place.extensions

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

fun String.asClassifyResult(sentence: String, color: Int): Spannable {
    val words = sentence.split(this)
    val prev = if (words.isNullOrEmpty()) {
        ""
    } else {
        words[0]
    }
    val post = if (words.count() < 2) {
        ""
    } else {
        words[1]
    }
    val spannable = SpannableString(prev + this + post)
    spannable.setSpan(ForegroundColorSpan(color), prev.length, (prev+this).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannable
}