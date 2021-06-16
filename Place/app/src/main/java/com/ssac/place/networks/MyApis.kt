package com.ssac.place.networks

import com.google.gson.GsonBuilder
import com.ssac.place.models.MyLike
import com.ssac.place.models.MyReview
import com.ssac.place.models.PlaceReview
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface MyApis {
    @FormUrlEncoded
    @POST("/user/login")
    fun loginWithKaKao(
            @Header("Authorization") token: String,
            @Field("type") type: String = "kakao"
    ): Call<LoginResponse>

    @Multipart
    @POST("/place/upload")
    fun classify(
        @Part image: MultipartBody.Part,
    ): Call<MyClassifyResponse>

    @FormUrlEncoded
    @POST("/place/review")
    fun createKakaoReview(
        @Header("Authorization") myToken: String,
        @Field("type") type: String,
        @Field("place_id") placeId: Int,
        @Field("place_name") placeName: String,
        @Field("place_url") placeUrl: String,
        @Field("category_name") categoryName: String,
        @Field("address_name") addressName: String,
        @Field("road_address_name") roadAddressName: String,
        @Field("phone") phone: String,
        @Field("category_group_code") categoryGroupCode: String,
        @Field("category_group_name") categoryGroupName: String,
        @Field("x") x: String,
        @Field("y") y: String,
        @Field("grade") rating: Int,
        @Field("text") contents: String
    ): Call<Unit>

    @FormUrlEncoded
    @POST("/place/review")
    fun createTourReview(
        @Header("Authorization") myToken: String,
        @Field("type") type: String,
        @Field("place_id") placeId: Int,
        @Field("addr1") addr1: String,
        @Field("addr2") addr2: String,
        @Field("areacode") areaCode: String,
        @Field("cat1") cat1: String,
        @Field("cat2") cat2: String,
        @Field("cat3") cat3: String,
        @Field("content_type_id") contentTypeId: String,
        @Field("createdtime") createdTime: String,
        @Field("firstimage") firstImage: String,
        @Field("firstImage2") firstImage2: String,
        @Field("mapx") mapX: String,
        @Field("mapy") mapY: String,
        @Field("modifiedtime") modifiedTime: String,
        @Field("sigungucode") sigungucode: String,
        @Field("tel") tel: String,
        @Field("title") title: String,
        @Field("overview") overview: String,
        @Field("zipcode") zipCode: String,
        @Field("homepage") homepage: String,
        @Field("grade") rating: Int,
        @Field("text") contents: String
    ): Call<Unit>

    @PATCH("/place/review")
    fun patchReview(
            @Header("Authorization") myToken: String,
            @Field("review_id") reviewId: String,
            @Field("grade") rating: Int,
            @Field("text") contents: String
    ): Call<Unit>

    @GET("/place/{placeId}/review")
    fun fetchPlaceReviewList(
            @Path("placeId") placeId: Int,
            @Query("type") type: String,
    ): Call<FetchPlaceReviewListResponse>

    @GET("/place/review")
    fun fetchMyReviewList(
        @Header("Authorization") myToken: String
    ): Call<FetchMyReviewListResponse>

    @FormUrlEncoded
    @POST("/place/like")
    fun createTourLike(
            @Header("Authorization") myToken: String,
            @Field("type") type: String = "tour",
            @Field("place_id") placeId: Int,
            @Field("addr1") addr1: String,
            @Field("addr2") addr2: String,
            @Field("areacode") areaCode: String,
            @Field("cat1") cat1: String,
            @Field("cat2") cat2: String,
            @Field("cat3") cat3: String,
            @Field("content_type_id") contentTypeId: String,
            @Field("createdtime") createdTime: String,
            @Field("firstimage") firstImage: String,
            @Field("firstImage2") firstImage2: String,
            @Field("mapx") mapX: String,
            @Field("mapy") mapY: String,
            @Field("modifiedtime") modifiedTime: String,
            @Field("sigungucode") sigungucode: String,
            @Field("tel") tel: String,
            @Field("title") title: String,
            @Field("overview") overview: String,
            @Field("zipcode") zipCode: String,
            @Field("homepage") homepage: String
    ): Call<Unit>

    @GET("/place/user/like")
    fun fetchMyLikeList(
            @Header("Authorization") myToken: String
    ): Call<FetchMyLikeListResponse>

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

data class LoginResponse(
    val id: Int,
    val social_type: String,
    val nickname: String
)

data class MyClassifyResponse(
    val name: String,
    val sentence: String
)

data class FetchPlaceReviewListResponse(
        val reviews: List<PlaceReview>,
        val grade: String
)

data class FetchMyReviewListResponse(
        val reviews: List<MyReview>
)

data class FetchMyLikeListResponse(
        val all: List<MyLike>,
        val a12: List<MyLike>,
        val a14: List<MyLike>,
        val a15: List<MyLike>,
        val a28: List<MyLike>,
        val a32: List<MyLike>,
        val a38: List<MyLike>,
        val a39: List<MyLike>
)