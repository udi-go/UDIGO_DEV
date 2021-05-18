package com.ssac.place

import com.ssac.place.models.TravelDetail
import com.ssac.place.models.TravelRecommend
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.Serializable

interface TravelApis {
    @GET("/openapi/service/rest/KorService/locationBasedList")
    fun search(
        @Query("mapX") longitude: Double,
        @Query("mapY") latitude: Double,
        @Query("radius") radius: Int = 20000,
        @Query("MobileApp") app: String = "uhDGo",
        @Query("MobileOS") os: String = "AND",
        @Query("ServiceKey") serviceKey: String = "nudDGaDjaOXRBLMi3/QiVUrKTpQ+IT+2Ctpax4vr0pxg8w3Z46E309qU2Df1ZBTIrcjv2h0plutjo9jn5PmLeA==",
    ): Call<TravelSearchResponse>

    @GET("/openapi/service/rest/KorService/detailCommon")
    fun detail(
        @Query("contentId") contentId: String,
        @Query("MobileApp") app: String = "uhDGo",
        @Query("MobileOS") os: String = "AND",
        @Query("defaultYN") defaultYN: String = "Y",
        @Query("firstImageYN") firstImageYN: String = "Y",
        @Query("areacodeYN") areacodeYN: String = "Y",
        @Query("catcodeYN") catcodeYN: String = "Y",
        @Query("addrinfoYN") addrinfoYN: String = "Y",
        @Query("mapinfoYN") mapinfoYN: String = "Y",
        @Query("overviewYN") overviewYN: String = "Y",
        @Query("ServiceKey") serviceKey: String = "nudDGaDjaOXRBLMi3/QiVUrKTpQ+IT+2Ctpax4vr0pxg8w3Z46E309qU2Df1ZBTIrcjv2h0plutjo9jn5PmLeA==",
    ): Call<TravelDetailResponse>

    companion object {
        private var instance: TravelApis? = null

        private const val BASE_URL = "http://api.visitkorea.or.kr/"
        fun getInstance(): TravelApis {
            if (instance == null) {
                instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(
                        OkHttpClient.Builder()
                            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(
                                HttpLoggingInterceptor.Level.BODY) })
                            .build()
                    )
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build()
                    .create(TravelApis::class.java)
            }
            return instance!!
        }
    }
}

@Root(name = "response", strict = false)
data class TravelSearchResponse @JvmOverloads constructor(
    @field:Element(name = "body")
    @param:Element(name = "body")
    val body: TravelSearchResponseBody? = null
): Serializable

@Root(name = "body", strict = false)
data class TravelSearchResponseBody @JvmOverloads constructor(
    @field:ElementList(name = "items")
    @param:ElementList(name = "items")
    val items: List<TravelRecommend>? = null
): Serializable


@Root(name = "response", strict = false)
data class TravelDetailResponse @JvmOverloads constructor(
    @field:Element(name = "body")
    @param:Element(name = "body")
    val body: TravelDetailResponseBody? = null
): Serializable

@Root(name = "body", strict = false)
data class TravelDetailResponseBody @JvmOverloads constructor(
    @field:ElementList(name = "items")
    @param:ElementList(name = "items")
    val items: List<TravelDetail>? = null
): Serializable
