package com.ssac.place.models

import android.text.Spannable
import androidx.core.text.HtmlCompat
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.io.Serializable


@Root(name = "item", strict = false)
data class TravelDetail @JvmOverloads constructor (

    @field:Element(name = "contentid", required = false)
    @param:Element(name = "contentid", required = false)
    val contentid: String,

    @field:Element(name = "contenttypeid", required = false)
    @param:Element(name = "contenttypeid", required = false)
    val contentTypeId: String? = null, // 12: 관광지, 14: 문화시설, 15: 축제공연행사, 25: 여행코스, 28: 레포츠, 32: 숙박, 38: 쇼핑, 39: 음식

    @field:Element(name = "firstimage", required = false)
    @param:Element(name = "firstimage", required = false)
    val firstimage: String? = null,

    @field:Element(name = "firstimage2", required = false)
    @param:Element(name = "firstimage2", required = false)
    val firstimage2: String? = null,

    @field:Element(name = "mapx", required = false)
    @param:Element(name = "mapx", required = false)
    val mapx: String? = null,

    @field:Element(name = "mapy", required = false)
    @param:Element(name = "mapy", required = false)
    val mapy: String? = null,

    @field:Element(name = "tel", required = false)
    @param:Element(name = "tel", required = false)
    val tel: String? = null,

    @field:Element(name = "title", required = false)
    @param:Element(name = "title", required = false)
    val title: String,

    @field:Element(name = "addr1", required = false)
    @param:Element(name = "addr1", required = false)
    val addr1: String? = null,

    @field:Element(name = "addr2", required = false)
    @param:Element(name = "addr2", required = false)
    val addr2: String? = null,

    @field:Element(name = "homepage", required = false)
    @param:Element(name = "homepage", required = false)
    val homepage: String? = null,

    @field:Element(name = "overview", required = false)
    @param:Element(name = "overview", required = false)
    val overview: String? = null,

    @field:Element(name = "createdtime", required = false)
    @param:Element(name = "createdtime", required = false)
    val createdtime: String,

    @field:Element(name = "modifiedtime", required = false)
    @param:Element(name = "modifiedtime", required = false)
    val modifiedtime: String,

    @field:Element(name = "areacode", required = false)
    @param:Element(name = "areacode", required = false)
    val areacode: String? = null,

    @field:Element(name = "sigungucode", required = false)
    @param:Element(name = "sigungucode", required = false)
    val sigungucode: String? = null,

    @field:Element(name = "cat1", required = false)
    @param:Element(name = "cat1", required = false)
    val cat1: String? = null,

    @field:Element(name = "cat2", required = false)
    @param:Element(name = "cat2", required = false)
    val cat2: String? = null,

    @field:Element(name = "cat3", required = false)
    @param:Element(name = "cat3", required = false)
    val cat3: String? = null,

    @field:Element(name = "zipcode", required = false)
    @param:Element(name = "zipcode", required = false)
    val zipcode: String? = null,

    ): Serializable {
    fun address(): String {
        var result = ""
        if (!addr1.isNullOrEmpty()) {
            result += addr1
        }
        if (!addr2.isNullOrEmpty()) {
            result += addr2
        }
        return result
    }

    fun homepage(): String {
        return if (homepage.isNullOrEmpty()) {
            "-"
        } else {
            homepage
        }
    }

    fun tel(): String {
        return if (tel.isNullOrEmpty()) {
            "-"
        } else {
            tel
        }
    }
}