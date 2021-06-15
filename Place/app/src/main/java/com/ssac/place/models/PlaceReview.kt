package com.ssac.place.models

data class PlaceReview(
    val review_id: String,
    val user_id: String,
    val user_nickname: String,
    val grade: String,
    val text: String,
    val date: String
)