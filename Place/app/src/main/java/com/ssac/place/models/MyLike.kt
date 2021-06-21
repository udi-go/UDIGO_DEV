package com.ssac.place.models

data class MyLike (
        val place_id: String,
        val type: String,
        val content_type_id: String,
        val title: String,
        val image: String,
        val address: String,
        val created_at: String,
        val mapx: String,
        val mapy: String
)