package com.ssac.place.models

data class MyReview(
    val review_id: String,
    val type: String,
    val place_id: String,
    val grade: Int,
    val text: String,
    val date: String,
    val place_title: String,
    val address: String,
    val image: String,

    val place_url: String,
    val mapx: String,
    val mapy: String,
    val category_name: String,
    val road_address_name: String,
    val phone: String,
    val category_group_code: String,
    val category_group_name: String
) {
    fun displayDate(): String {
        return date.substring(0, 10).replace("-", ".")
    }

    fun getKakaoDocument(): KakaoDocument {
        return KakaoDocument(place_id, place_title, category_name, category_group_code, category_group_name, phone, address, road_address_name, mapx, mapy, place_url)
    }
}