package com.ssac.place.networks

import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface MyApis {

    @POST("/login")
    fun login(
        @Body token: String
    )

    @Multipart
    @POST("/place/upload")
    fun classify(
        @Part image: MultipartBody.Part,
    ): Call<MyClassifyResponse>

    companion object {
        private var instance: MyApis? = null
        private val gson = GsonBuilder().create()

        private const val BASE_URL = "http://192.168.10.240:8000/"
        fun getInstance(): MyApis {
            if (instance == null) {
                instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(
                        OkHttpClient.Builder()
                            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(
                                HttpLoggingInterceptor.Level.BODY) })
                            .build()
                    )
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(MyApis::class.java)
            }
            return instance!!
        }
    }
}

data class MyClassifyResponse(
    val name: String
)