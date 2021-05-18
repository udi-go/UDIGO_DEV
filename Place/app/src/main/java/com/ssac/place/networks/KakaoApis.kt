package com.ssac.place

import com.google.gson.GsonBuilder
import com.ssac.place.models.KakaoDocument
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoApis {

    @GET("/v2/local/search/keyword.json")
    fun search(
        @Query("query") query: String,
        @Query("x") longitude: String,
        @Query("y") latitude: String,
        @Query("radius") radius: Int = 20000
    ): Call<KakaoSearchResponse>

    companion object {
        private var instance: KakaoApis? = null
        private val gson = GsonBuilder().create()

        private const val BASE_URL = "https://dapi.kakao.com/"
        fun getInstance(): KakaoApis {
            if (instance == null) {
                instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(
                        OkHttpClient.Builder()
                            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
                            .addInterceptor(KakaoHeaderInterceptor())
                            .build()
                    )
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(KakaoApis::class.java)
            }
            return instance!!
        }
    }
}

class KakaoHeaderInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val origin = chain.request()
        val request = origin.newBuilder()
            .addHeader("Authorization", "KakaoAK f66e6a3cd0f88030674d6888eec72dc7")
            .build()
        return chain.proceed(request)
    }
}

data class KakaoSearchResponse(
    val documents: List<KakaoDocument>
)