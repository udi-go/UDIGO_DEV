package com.ssac.place.models

import com.ssac.place.R
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import java.io.Serializable

data class KakaoDocument(
    val id: String,
    val place_name: String,
    val category_name: String,
    val category_group_code: String,
    val category_group_name: String,
    val phone: String,
    val address_name: String,
    val road_address_name: String,
    val x: String,
    val y: String,
    val place_url: String
): Serializable {
    fun address(): String {
        return if (road_address_name.isEmpty()) {
            address_name
        } else {
            road_address_name
        }
    }

    fun tel(): String {
        return if (phone.isEmpty()) {
            "-"
        } else {
            phone
        }
    }

    fun homepage(): String {
        return if (place_url.isEmpty()) {
            "-"
        } else {
            place_url
        }
    }

    fun toPOIItem(position:Int): MapPOIItem {
        return MapPOIItem().apply {
            tag = position
            itemName = place_name
            mapPoint = MapPoint.mapPointWithGeoCoord(y.toDouble(), x.toDouble())
            markerType = MapPOIItem.MarkerType.CustomImage
            selectedMarkerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.ic_gray_marker
            customSelectedImageResourceId = R.drawable.ic_green_marker
            isCustomImageAutoscale = true
            setCustomImageAnchor(0.5f, 1.0f)
        }
    }
}