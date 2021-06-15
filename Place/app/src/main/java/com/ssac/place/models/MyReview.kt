package com.ssac.place.models

data class MyReview(
    val review_id: String,
    val type: String,
    val place_id: String,
    val grade: Int,
    val text: String,
    val date: String,
    val place_title: String,
    val address: String
)